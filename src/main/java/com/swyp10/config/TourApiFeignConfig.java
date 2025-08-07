package com.swyp10.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import feign.Request;
import feign.RequestInterceptor;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
@Slf4j
public class TourApiFeignConfig {

    /**
     * Spring Boot의 기본 HttpMessageConverters 사용
     */
    @Bean
    public Decoder decoder(ObjectFactory<HttpMessageConverters> messageConverters) {
        return new SpringDecoder(messageConverters);
    }

    /**
     * XML 처리를 위한 HttpMessageConverter 추가
     */
    @Bean
    @Primary
    public HttpMessageConverters feignHttpMessageConverter() {

        // JSON 매퍼
        ObjectMapper jsonMapper = new ObjectMapper();
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        // XML 매퍼
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return new HttpMessageConverters(
            new MappingJackson2HttpMessageConverter(jsonMapper),
            new MappingJackson2XmlHttpMessageConverter(xmlMapper),
            new StringHttpMessageConverter(StandardCharsets.UTF_8)
        );
    }

    /**
     * Accept 헤더 설정
     */
    @Bean
    public RequestInterceptor tourApiRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Accept", "application/json, application/xml, text/xml, */*");
            requestTemplate.header("User-Agent", "swyp10-festival-app");
            log.debug("TourAPI request: {}", requestTemplate.url());
        };
    }

    /**
     * 타임아웃 설정
     */
    @Bean
    public Request.Options tourApiOptions() {
        return new Request.Options(
            Duration.ofSeconds(30),
            Duration.ofSeconds(120),
            true
        );
    }

    /**
     * 재시도 설정
     */
    @Bean
    public Retryer tourApiRetryer() {
        return new Retryer.Default(
            Duration.ofSeconds(2).toMillis(),
            Duration.ofSeconds(5).toMillis(),
            3
        );
    }

    /**
     * 에러 디코더
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            log.warn("TourAPI error - Method: {}, Status: {}, Reason: {}",
                methodKey, response.status(), response.reason());

            if (response.status() >= 500 || response.status() == 408) {
                return new RetryableException(
                    response.status(),
                    "TourAPI server error: " + response.reason(),
                    response.request().httpMethod(),
                    (Long)null,
                    response.request()
                );
            }

            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}
