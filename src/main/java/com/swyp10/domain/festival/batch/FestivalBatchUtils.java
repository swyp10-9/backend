package com.swyp10.domain.festival.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class FestivalBatchUtils {

    private final ObjectMapper objectMapper;

    /**
     * 중첩된 Map 구조에서 안전하게 특정 key 경로의 Map을 얻는다.
     * @param map 최상위 Map
     * @param keys 순차적인 key 경로
     * @return 최종 Map (없으면 빈 Map 반환)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getNestedMap(Map<String, Object> map, String... keys) {
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

    /**
     * Map 데이터를 SearchFestival2Dto로 변환
     */
    public SearchFestival2Dto parseSearchFestival2Dto(Map<String, Object> item) {
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

    /**
     * Map 데이터를 DetailCommon2Dto로 변환
     */
    public DetailCommon2Dto parseDetailCommon2Dto(Map<String, Object> response) {
        Map<String, Object> body = getNestedMap(response, "response", "body");
        if (body.get("items") instanceof String) return new DetailCommon2Dto();

        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return new DetailCommon2Dto();

        Object itemObj = items.get("item");
        Map<String, Object> firstItem;

        if (itemObj instanceof List<?>) {
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemObj;
            if (itemList.isEmpty()) {
                return new DetailCommon2Dto();
            }
            firstItem = itemList.get(0);
        } else if (itemObj instanceof Map<?, ?>) {
            firstItem = (Map<String, Object>) itemObj;
        } else {
            return new DetailCommon2Dto();
        }

        return DetailCommon2Dto.builder()
            .overview(getStr(firstItem, "overview"))
            .build();
    }

    /**
     * Map 데이터를 DetailIntro2Dto로 변환
     */
    public DetailIntro2Dto parseDetailIntro2Dto(Map<String, Object> response) {
        Map<String, Object> body = getNestedMap(response, "response", "body");
        if (body.get("items") instanceof String) return new DetailIntro2Dto();

        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return new DetailIntro2Dto();

        Object itemObj = items.get("item");
        Map<String, Object> firstItem;

        if (itemObj instanceof List<?>) {
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) itemObj;
            if (itemList.isEmpty()) {
                return new DetailIntro2Dto();
            }
            firstItem = itemList.get(0);
        } else if (itemObj instanceof Map<?, ?>) {
            firstItem = (Map<String, Object>) itemObj;
        } else {
            return new DetailIntro2Dto();
        }

        return DetailIntro2Dto.builder()
            .agelimit(getStr(firstItem, "agelimit"))
            .bookingplace(getStr(firstItem, "bookingplace"))
            .discountinfofestival(getStr(firstItem, "discountinfofestival"))
            .eventhomepage(getStr(firstItem, "eventhomepage"))
            .eventplace(getStr(firstItem, "eventplace"))
            .festivalgrade(getStr(firstItem, "festivalgrade"))
            .placeinfo(getStr(firstItem, "placeinfo"))
            .playtime(getStr(firstItem, "playtime"))
            .program(getStr(firstItem, "program"))
            .spendtimefestival(getStr(firstItem, "spendtimefestival"))
            .sponsor1(getStr(firstItem, "sponsor1"))
            .sponsor1tel(getStr(firstItem, "sponsor1tel"))
            .sponsor2(getStr(firstItem, "sponsor2"))
            .sponsor2tel(getStr(firstItem, "sponsor2tel"))
            .subevent(getStr(firstItem, "subevent"))
            .usetimefestival(getStr(firstItem, "usetimefestival"))
            .build();
    }

    /**
     * Map 데이터를 List<DetailImage2Dto>로 변환
     */
    public List<DetailImage2Dto> parseDetailImageList2Dto(Map<String, Object> response) {
        Map<String, Object> body = getNestedMap(response, "response", "body");
        if (body.get("items") instanceof String) return Collections.emptyList();

        Object itemsObj = body.get("items");
        if (itemsObj == null || "null".equals(itemsObj)) {
            return Collections.emptyList();
        }
        if (!(itemsObj instanceof Map)) {
            return Collections.emptyList();
        }

        Map<String, Object> items = (Map<String, Object>) itemsObj;
        Object itemObj = items.get("item");
        if (itemObj instanceof List<?>) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) itemObj;
            List<DetailImage2Dto> images = new ArrayList<>();
            for (Map<String, Object> map : list) {
                images.add(parseDetailImage2Dto(map));
            }
            return images;
        } else if (itemObj instanceof Map<?, ?>) {
            return List.of(parseDetailImage2Dto((Map<String, Object>) itemObj));
        }
        return Collections.emptyList();
    }

    /**
     * Map 데이터를 DetailImage2Dto로 변환
     */
    public DetailImage2Dto parseDetailImage2Dto(Map<String, Object> item) {
        return DetailImage2Dto.builder()
            .imgid(getStr(item, "imgid"))
            .originimgurl(getStr(item, "originimgurl"))
            .smallimageurl(getStr(item, "smallimageurl"))
            .serialnum(getStr(item, "serialnum"))
            .build();
    }

    /**
     * Map에서 안전하게 String을 추출
     */
    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? null : String.valueOf(val);
    }

    public List<SearchFestival2Dto> parseFestivalList(Map<String, Object> response) {
        try {
            Map<String, Object> body = getNestedMap(response, "response", "body");
            Map<String, Object> items = (Map<String, Object>) body.get("items");

            if (items == null) return Collections.emptyList();

            Object itemObj = items.get("item");
            List<Map<String, Object>> festivalList;

            if (itemObj instanceof List<?>) {
                festivalList = (List<Map<String, Object>>) itemObj;
            } else if (itemObj instanceof Map<?,?>) {
                festivalList = List.of((Map<String, Object>) itemObj);
            } else {
                return Collections.emptyList();
            }

            return festivalList.stream()
                .map(this::parseSearchFestival2Dto)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Failed to parse festival list", e);
            return Collections.emptyList();
        }
    }

    public int extractTotalCount(Map<String, Object> response) {
        try {
            Map<String, Object> body = getNestedMap(response, "response", "body");
            return Integer.parseInt(String.valueOf(body.get("totalCount")));
        } catch (Exception e) {
            log.warn("Failed to extract total count", e);
            return 0;
        }
    }
}
