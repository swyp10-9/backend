package com.swyp10.global.advice;

import com.swyp10.global.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class CommonResponseAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private HttpServletRequest request;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        String uri = request.getRequestURI();
        // Swagger 및 Actuator, Health Check 등은 래핑하지 않음
        return !uri.startsWith("/v3/api-docs")
            && !uri.startsWith("/swagger-ui")
            && !uri.startsWith("/swagger-resources")
            && !uri.startsWith("/actuator");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {
        if (body instanceof CommonResponse) return body;
        return CommonResponse.success(body);
    }
}
