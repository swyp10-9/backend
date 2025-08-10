package com.swyp10.config;

import com.swyp10.domain.festival.enums.FestivalPeriod;
import com.swyp10.domain.festival.enums.FestivalStatus;
import com.swyp10.domain.festival.enums.FestivalTheme;
import com.swyp10.domain.festival.enums.FestivalWithWhom;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // favicon.ico 요청 무시 (404 반환)
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Festival 관련 Enum Converter 등록
        registry.addConverter(new StringToFestivalPeriodConverter());
        registry.addConverter(new StringToFestivalStatusConverter());
        registry.addConverter(new StringToFestivalWithWhomConverter());
        registry.addConverter(new StringToFestivalThemeConverter());
    }

    /**
     * FestivalPeriod Converter
     */
    public static class StringToFestivalPeriodConverter implements Converter<String, FestivalPeriod> {
        @Override
        public FestivalPeriod convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return FestivalPeriod.ALL;
            }

            // 대소문자 구분 없이 변환
            String normalizedSource = source.trim().toUpperCase();

            try {
                // ENUM 이름으로 직접 매칭 시도
                return FestivalPeriod.valueOf(normalizedSource);
            } catch (IllegalArgumentException e) {
                // displayName으로 매칭 시도
                for (FestivalPeriod period : FestivalPeriod.values()) {
                    if (period.getDisplayName().equalsIgnoreCase(source.trim())) {
                        return period;
                    }
                }

                // snake_case를 UPPER_CASE로 변환 (this_week -> THIS_WEEK)
                String upperCaseSource = normalizedSource.replace('-', '_');
                try {
                    return FestivalPeriod.valueOf(upperCaseSource);
                } catch (IllegalArgumentException ex) {
                    // 기본값 반환
                    return FestivalPeriod.ALL;
                }
            }
        }
    }

    /**
     * FestivalStatus Converter
     */
    public static class StringToFestivalStatusConverter implements Converter<String, FestivalStatus> {
        @Override
        public FestivalStatus convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return FestivalStatus.ALL;
            }

            String normalizedSource = source.trim().toUpperCase();

            try {
                return FestivalStatus.valueOf(normalizedSource);
            } catch (IllegalArgumentException e) {
                for (FestivalStatus status : FestivalStatus.values()) {
                    if (status.getDisplayName().equalsIgnoreCase(source.trim())) {
                        return status;
                    }
                }
                return FestivalStatus.ALL;
            }
        }
    }

    /**
     * FestivalWithWhom Converter
     */
    public static class StringToFestivalWithWhomConverter implements Converter<String, FestivalWithWhom> {
        @Override
        public FestivalWithWhom convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return FestivalWithWhom.ALL;
            }

            String normalizedSource = source.trim().toUpperCase();

            try {
                return FestivalWithWhom.valueOf(normalizedSource);
            } catch (IllegalArgumentException e) {
                for (FestivalWithWhom withWhom : FestivalWithWhom.values()) {
                    if (withWhom.getDisplayName().equalsIgnoreCase(source.trim())) {
                        return withWhom;
                    }
                }
                return FestivalWithWhom.ALL;
            }
        }
    }

    /**
     * FestivalTheme Converter
     */
    public static class StringToFestivalThemeConverter implements Converter<String, FestivalTheme> {
        @Override
        public FestivalTheme convert(String source) {
            if (source == null || source.trim().isEmpty()) {
                return FestivalTheme.ALL;
            }

            String normalizedSource = source.trim().toUpperCase();

            try {
                return FestivalTheme.valueOf(normalizedSource);
            } catch (IllegalArgumentException e) {
                for (FestivalTheme theme : FestivalTheme.values()) {
                    if (theme.getDisplayName().equalsIgnoreCase(source.trim())) {
                        return theme;
                    }
                }
                return FestivalTheme.ALL;
            }
        }
    }

}

