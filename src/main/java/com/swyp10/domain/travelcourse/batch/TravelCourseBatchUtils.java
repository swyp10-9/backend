package com.swyp10.domain.travelcourse.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.travelcourse.dto.tourapi.DetailInfoCourseDto;
import com.swyp10.domain.travelcourse.dto.tourapi.SearchTravelCourseDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
                .serialnum(getStr(item, "serialnum"))
                .coursename(getStr(item, "coursename"))
                .coursedesc(getStr(item, "coursedesc"))
                .coursedist(getStr(item, "coursedist"))
                .coursestime(getStr(item, "coursestime"))
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
}
