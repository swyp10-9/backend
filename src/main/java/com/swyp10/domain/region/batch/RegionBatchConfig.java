package com.swyp10.domain.region.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.region.entity.AreaCode;
import com.swyp10.domain.region.entity.LdongCode;
import com.swyp10.domain.region.service.AreaCodeService;
import com.swyp10.domain.region.service.LdongCodeService;
import com.swyp10.domain.festival.client.TourApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class RegionBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TourApiClient tourApiClient;
    private final AreaCodeService areaCodeService;
    private final LdongCodeService ldongCodeService;
    private final ObjectMapper objectMapper;

    @Value("${tourapi.service-key}")
    private String SERVICE_KEY;

    @Bean
    public Job regionSyncJob(Step regionSyncStep) {
        return new JobBuilder("regionSyncJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .flow(regionSyncStep)
            .end()
            .build();
    }

    @Bean
    public Step regionSyncStep(Tasklet regionSyncTasklet) {
        return new StepBuilder("regionSyncStep", jobRepository)
            .tasklet(regionSyncTasklet, transactionManager)
            .build();
    }

    @Bean
    public Tasklet regionSyncTasklet() {
        return (contribution, chunkContext) -> {
            // 1. areaCode2 API 호출 및 데이터 저장
            Map<String, Object> areaCodeResponse = tourApiClient.areaCode2(
                SERVICE_KEY, "ETC", "swyp10", "json"
            );

            List<AreaCode> areaCodes = parseAreaCodeList(areaCodeResponse);
            areaCodeService.deleteAll();
            areaCodeService.saveAll(areaCodes);
            log.info("AreaCode 데이터 {}건 저장 완료", areaCodes.size());

            // 2. ldongCode2 API 호출 및 데이터 저장
            Map<String, Object> ldongCodeResponse = tourApiClient.ldongCode2(
                SERVICE_KEY, "ETC", "swyp10", "json"
            );

            List<LdongCode> ldongCodes = parseLdongCodeList(ldongCodeResponse);
            ldongCodeService.deleteAll();
            ldongCodeService.saveAll(ldongCodes);
            log.info("LdongCode 데이터 {}건 저장 완료", ldongCodes.size());

            return RepeatStatus.FINISHED;
        };
    }

    private List<AreaCode> parseAreaCodeList(Map<String, Object> response) {
        Map<String, Object> body = getNestedMap(response, "response", "body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return List.of();

        Object itemObj = items.get("item");
        List<Map<String, Object>> list;
        if (itemObj instanceof List<?>) {
            list = (List<Map<String, Object>>) itemObj;
        } else {
            list = List.of((Map<String, Object>) itemObj);
        }

        List<AreaCode> areaCodes = new ArrayList<>();
        for (Map<String, Object> item : list) {
            areaCodes.add(AreaCode.builder()
                .code((String) item.get("code"))
                .name((String) item.get("name"))
                .build());
        }
        return areaCodes;
    }

    private List<LdongCode> parseLdongCodeList(Map<String, Object> response) {
        Map<String, Object> body = getNestedMap(response, "response", "body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return List.of();

        Object itemObj = items.get("item");
        List<Map<String, Object>> list;
        if (itemObj instanceof List<?>) {
            list = (List<Map<String, Object>>) itemObj;
        } else {
            list = List.of((Map<String, Object>) itemObj);
        }

        List<LdongCode> ldongCodes = new ArrayList<>();
        for (Map<String, Object> item : list) {
            ldongCodes.add(LdongCode.builder()
                .code((String) item.get("code"))
                .name((String) item.get("name"))
                .sigunguCode((String) item.get("sigunguCode"))
                .areaCode((String) item.get("areacode"))
                .build());
        }
        return ldongCodes;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNestedMap(Map<String, Object> map, String... keys) {
        Map<String, Object> current = map;
        for (String key : keys) {
            Object value = current.get(key);
            if (value instanceof Map) {
                current = (Map<String, Object>) value;
            } else {
                return Collections.emptyMap();
            }
        }
        return current;
    }
}