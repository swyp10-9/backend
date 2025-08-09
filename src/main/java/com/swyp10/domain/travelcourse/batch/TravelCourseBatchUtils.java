package com.swyp10.domain.travelcourse.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class TravelCourseBatchUtils {

    private final ObjectMapper objectMapper;

    public TravelCourseBatchUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SearchTravelCourseDto parseSearchTravelCourseDto(Map<String, Object> item) {
        return SearchTravelCourseDto.builder()
            .addr1(getStr(item, "addr1"))
            .areacode(getStr(item, "areacode"))
            .contentid(getStr(item, "contentid"))
            .contenttypeid(getStr(item, "contenttypeid"))
            .createdtime(getStr(item, "createdtime"))
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
            .build();
    }

    public List<DetailInfoCourseDto> parseDetailInfoCourseList(Map<String, Object> response) {
        Map<String, Object> body = getNestedMap(response, "response", "body");
        if (body.get("items") instanceof String) return Collections.emptyList();

        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return Collections.emptyList();

        Object itemObj = items.get("item");
        List<Map<String, Object>> list;
        if (itemObj instanceof List<?>) {
            list = (List<Map<String, Object>>) itemObj;
        } else {
            list = List.of((Map<String, Object>) itemObj);
        }

        List<DetailInfoCourseDto> detailInfoList = new ArrayList<>();
        for (Map<String, Object> item : list) {
            detailInfoList.add(DetailInfoCourseDto.builder()
                .subnum(getStr(item, "subnum"))
                .subcontentid(getStr(item, "subcontentid"))
                .subname(getStr(item, "subname"))
                .subdetailoverview(getStr(item, "subdetailoverview"))
                .subdetailimg(getStr(item, "subdetailimg"))
                .build());
        }
        return detailInfoList;
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? null : String.valueOf(val);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getNestedMap(Map<String, Object> map, String... keys) {
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

    public List<SearchTravelCourseDto> parseTravelCourseList(Map<String, Object> response) {
        try {
            Map<String, Object> body = getNestedMap(response, "response", "body");
            Map<String, Object> items = (Map<String, Object>) body.get("items");

            if (items == null || items.get("item") == null) {
                return Collections.emptyList();
            }

            Object itemObj = items.get("item");
            List<Map<String, Object>> courseList;

            if (itemObj instanceof List<?>) {
                courseList = (List<Map<String, Object>>) itemObj;
            } else {
                courseList = List.of((Map<String, Object>) itemObj);
            }

            return courseList.stream()
                .map(this::parseSearchTravelCourseDto)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Failed to parse travel course list", e);
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
