package com.swyp10.domain.festival.batch;

import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import com.swyp10.domain.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FestivalBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FestivalService festivalService;
    private final RestTemplate restTemplate;

    // 인증키(임의값으로 표기, 실제 운영키로 교체)
    private static final String SERVICE_KEY = "MY-TOURAPI-KEY-1234";

    @Bean
    public Job festivalSyncJob(Step festivalSyncStep) {
        return new JobBuilder("festivalSyncJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .flow(festivalSyncStep)
            .end()
            .build();
    }

    @Bean
    public Step festivalSyncStep(Tasklet festivalSyncTasklet) {
        return new StepBuilder("festivalSyncStep", jobRepository)
            .tasklet(festivalSyncTasklet, transactionManager)
            .build();
    }

    @Bean
    public Tasklet festivalSyncTasklet() {
        return (contribution, chunkContext) -> {
            // 1. 전국 모든 행사(축제) 목록 검색 (페이징)
            int page = 1;
            int totalCount;
            int pageSize = 100;
            do {
                String url = buildSearchFestival2Url(page, pageSize);
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                Map<String, Object> body = getNestedMap(response, "response", "body");
                totalCount = Integer.parseInt(String.valueOf(body.get("totalCount")));

                Map<String, Object> items = (Map<String, Object>) body.get("items");
                if (items == null) break;

                List<Map<String, Object>> festivalList;
                Object itemObj = items.get("item");
                if (itemObj instanceof List<?>) {
                    festivalList = (List<Map<String, Object>>) itemObj;
                } else if (itemObj instanceof Map<?,?>) {
                    festivalList = List.of((Map<String, Object>) itemObj);
                } else {
                    break;
                }

                for (Map<String, Object> item : festivalList) {
                    SearchFestival2Dto searchDto = parseSearchFestival2Dto(item);
                    // detail API 호출
                    DetailCommon2Dto commonDto = callDetailCommon2(searchDto.getContentid());
                    DetailIntro2Dto introDto = callDetailIntro2(searchDto.getContentid());
                    List<DetailImage2Dto> images = callDetailImage2(searchDto.getContentid());
                    // 저장(UPSERT)
                    festivalService.saveOrUpdateFestival(searchDto, commonDto, introDto, images);
                }
                page++;
            } while ((page - 1) * pageSize < totalCount);

            return RepeatStatus.FINISHED;
        };
    }

    private String buildSearchFestival2Url(int pageNo, int numOfRows) {
        return "http://apis.data.go.kr/B551011/KorService2/searchFestival2"
            + "?serviceKey=" + SERVICE_KEY
            + "&MobileOS=ETC"
            + "&MobileApp=swyp10"
            + "&_type=json"
            + "&numOfRows=" + numOfRows
            + "&pageNo=" + pageNo;
    }

    private SearchFestival2Dto parseSearchFestival2Dto(Map<String, Object> item) {
        return SearchFestival2Dto.builder()
            .contentid(getStr(item, "contentid"))
            .addr1(getStr(item, "addr1"))
            .areacode(getStr(item, "areacode"))
            .contenttypeid(getStr(item, "contenttypeid"))
            .createdtime(getStr(item, "createdtime"))
            .eventstartdate(getStr(item, "eventstartdate"))
            .eventenddate(getStr(item, "eventenddate"))
            .firstimage(getStr(item, "firstimage"))
            .firstimage2(getStr(item, "firstimage2"))
            .mapx(getStr(item, "mapx"))
            .mapy(getStr(item, "mapy"))
            .modifiedtime(getStr(item, "modifiedtime"))
            .sigungucode(getStr(item, "sigungucode"))
            .tel(getStr(item, "tel"))
            .title(getStr(item, "title"))
            .lDongRegnCd(getStr(item, "lDongRegnCd"))
            .lDongSignguCd(getStr(item, "lDongSignguCd"))
            .lclsSystm1(getStr(item, "lclsSystm1"))
            .progresstype(getStr(item, "progresstype"))
            .festivaltype(getStr(item, "festivaltype"))
            .build();
    }

    private DetailCommon2Dto callDetailCommon2(String contentId) {
        String url = "http://apis.data.go.kr/B551011/KorService2/detailCommon2"
            + "?serviceKey=" + SERVICE_KEY
            + "&MobileOS=ETC"
            + "&MobileApp=swyp10"
            + "&_type=json"
            + "&contentId=" + URLEncoder.encode(contentId, StandardCharsets.UTF_8)
            + "&defaultYN=Y&overviewYN=Y";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, Object> body = getNestedMap(response, "response", "body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return new DetailCommon2Dto();
        Map<String, Object> item = (Map<String, Object>) items.get("item");
        return DetailCommon2Dto.builder()
            .overview(getStr(item, "overview"))
            .build();
    }

    private DetailIntro2Dto callDetailIntro2(String contentId) {
        String url = "http://apis.data.go.kr/B551011/KorService2/detailIntro2"
            + "?serviceKey=" + SERVICE_KEY
            + "&MobileOS=ETC"
            + "&MobileApp=swyp10"
            + "&_type=json"
            + "&contentId=" + URLEncoder.encode(contentId, StandardCharsets.UTF_8)
            + "&contentTypeId=15";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, Object> body = getNestedMap(response, "response", "body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return new DetailIntro2Dto();
        Map<String, Object> item = (Map<String, Object>) items.get("item");
        return DetailIntro2Dto.builder()
            .agelimit(getStr(item, "agelimit"))
            .bookingplace(getStr(item, "bookingplace"))
            .discountinfofestival(getStr(item, "discountinfofestival"))
            .eventhomepage(getStr(item, "eventhomepage"))
            .eventplace(getStr(item, "eventplace"))
            .festivalgrade(getStr(item, "festivalgrade"))
            .placeinfo(getStr(item, "placeinfo"))
            .playtime(getStr(item, "playtime"))
            .program(getStr(item, "program"))
            .spendtimefestival(getStr(item, "spendtimefestival"))
            .sponsor1(getStr(item, "sponsor1"))
            .sponsor1tel(getStr(item, "sponsor1tel"))
            .sponsor2(getStr(item, "sponsor2"))
            .sponsor2tel(getStr(item, "sponsor2tel"))
            .subevent(getStr(item, "subevent"))
            .usetimefestival(getStr(item, "usetimefestival"))
            .build();
    }

    private List<DetailImage2Dto> callDetailImage2(String contentId) {
        String url = "http://apis.data.go.kr/B551011/KorService2/detailImage2"
            + "?serviceKey=" + SERVICE_KEY
            + "&MobileOS=ETC"
            + "&MobileApp=swyp10"
            + "&_type=json"
            + "&contentId=" + URLEncoder.encode(contentId, StandardCharsets.UTF_8)
            + "&imageYN=Y&subImageYN=Y";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        Map<String, Object> body = getNestedMap(response, "response", "body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return List.of();

        Object itemObj = items.get("item");
        if (itemObj instanceof List<?>) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) itemObj;
            return list.stream().map(this::parseDetailImage2Dto).toList();
        } else if (itemObj instanceof Map<?,?>) {
            return List.of(parseDetailImage2Dto((Map<String, Object>) itemObj));
        }
        return List.of();
    }

    private DetailImage2Dto parseDetailImage2Dto(Map<String, Object> item) {
        return DetailImage2Dto.builder()
            .imgid(getStr(item, "imgid"))
            .originimgurl(getStr(item, "originimgurl"))
            .smallimageurl(getStr(item, "smallimageurl"))
            .serialnum(getStr(item, "serialnum"))
            .build();
    }

    // 유틸: Map 내부에서 String 안전 추출
    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? null : String.valueOf(val);
    }

    // 다단계 중첩 map 안전 추출
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
