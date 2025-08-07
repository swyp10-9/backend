package com.swyp10.domain.festival.client;

import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class TourApiFeignConfig {

    @Bean
    public Request.Options tourApiOptions() {
        return new Request.Options(
            Duration.ofSeconds(20),  // connectTimeout
            Duration.ofSeconds(90),  // readTimeout
            true                     // followRedirects
        );
    }

    @Bean
    public Retryer tourApiRetryer() {
        return new Retryer.Default(
            Duration.ofSeconds(2).toMillis(),
            Duration.ofSeconds(5).toMillis(),
            3
        );
    }

    @Bean
    public RequestInterceptor tourApiRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("User-Agent", "swyp10-festival-app");
            requestTemplate.header("Accept", "application/json");
            log.debug("TourAPI request: {}", requestTemplate.url());
        };
    }
}
