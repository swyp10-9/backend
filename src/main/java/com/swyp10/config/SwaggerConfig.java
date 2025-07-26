package com.swyp10.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
