package com.swyp10.config;

import com.swyp10.global.response.CommonResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "SWYP10 Backend API",
        version = "1.0.0",
        description = """
            # 공통 응답 구조(CommonResponse)

            모든 API 응답은 아래의 공통 구조로 래핑됩니다.

            ---

            ## ✅ 성공 응답 예시

            ```
            {
              "success": true,
              "data": { ... },    // API별 실제 응답 객체
              "message": null,
              "code": null,
              "errorDetail": null
            }
            ```

            ---

            ## ❌ 실패 응답 예시

            ```
            {
              "success": false,
              "data": null,
              "message": "오류 메시지",
              "code": 4000,
              "errorDetail": {
                // 필요시 필드별 상세 에러 정보 등
              }
            }
            ```

            - `success`: true(성공), false(실패)
            - `data`: 실제 응답 데이터(성공 시만)
            - `message`: 에러 메시지(실패 시)
            - `code`: 에러 코드(실패 시)
            - `errorDetail`: (옵션) 상세 에러 정보, 유효성 오류 등

            ---
            각 API의 실제 데이터 구조는 `data` 필드 안에 포함되어 있습니다.
            """
    )
)
@SecurityScheme(
    name = "Bearer Authentication",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("Bearer Authentication");
        
        return new OpenAPI()
                .addSecurityItem(securityRequirement);
    }

    @Bean
    public OperationCustomizer commonResponseCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // 기존 응답들을 CommonResponse로 감싸기
            ApiResponses responses = operation.getResponses();
            if (responses != null) {
                responses.forEach((status, response) -> {
                    Content content = response.getContent();
                    if (content != null) {
                        content.forEach((mediaTypeString, mediaType) -> {
                            if ("application/json".equals(mediaTypeString)) {
                                // 원본 스키마 가져오기
                                var originalSchema = mediaType.getSchema();
                                
                                if (originalSchema != null) {
                                    // CommonResponse로 감싸기
                                    try {
                                        // CommonResponse<T>의 스키마 생성
                                        Type commonResponseType = new ParameterizedType() {
                                            @Override
                                            public Type[] getActualTypeArguments() {
                                                return new Type[]{Object.class};
                                            }

                                            @Override
                                            public Type getRawType() {
                                                return CommonResponse.class;
                                            }

                                            @Override
                                            public Type getOwnerType() {
                                                return null;
                                            }
                                        };

                                        ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                                                .resolveAsResolvedSchema(new AnnotatedType(commonResponseType));
                                        
                                        if (resolvedSchema != null && resolvedSchema.schema != null) {
                                            // data 필드에 원본 스키마 설정
                                            if (resolvedSchema.schema.getProperties() != null && 
                                                resolvedSchema.schema.getProperties().containsKey("data")) {
                                                resolvedSchema.schema.getProperties().put("data", originalSchema);
                                            }
                                            
                                            mediaType.setSchema(resolvedSchema.schema);
                                        }
                                    } catch (Exception e) {
                                        // 스키마 변환 실패시 원본 유지
                                        System.err.println("Failed to wrap schema with CommonResponse: " + e.getMessage());
                                    }
                                }
                            }
                        });
                    }
                });
            }

            // 공통 에러 응답 추가 (아직 없는 경우)
            addCommonErrorResponses(operation);

            return operation;
        };
    }

    private void addCommonErrorResponses(Operation operation) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        // 400 Bad Request
        if (!responses.containsKey("400")) {
            responses.addApiResponse("400", createErrorResponse("잘못된 요청"));
        }

        // 401 Unauthorized  
        if (!responses.containsKey("401")) {
            responses.addApiResponse("401", createErrorResponse("인증이 필요합니다"));
        }

        // 403 Forbidden
        if (!responses.containsKey("403")) {
            responses.addApiResponse("403", createErrorResponse("접근 권한이 없습니다"));
        }

        // 500 Internal Server Error
        if (!responses.containsKey("500")) {
            responses.addApiResponse("500", createErrorResponse("서버 내부 오류"));
        }
    }

    private ApiResponse createErrorResponse(String description) {
        try {
            ResolvedSchema errorSchema = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(CommonResponse.class));
            
            MediaType mediaType = new MediaType();
            if (errorSchema != null && errorSchema.schema != null) {
                mediaType.setSchema(errorSchema.schema);
                
                // 에러 응답 예시 추가
                mediaType.example(CommonResponse.fail(description, 4000));
            }

            Content content = new Content();
            content.addMediaType("application/json", mediaType);

            return new ApiResponse()
                    .description(description + " (CommonResponse 구조로 래핑됨)")
                    .content(content);
        } catch (Exception e) {
            // 스키마 생성 실패시 기본 응답 반환
            return new ApiResponse().description(description);
        }
    }
}
