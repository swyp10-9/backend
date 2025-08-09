package com.swyp10.domain.restaurant.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailInfo2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailIntro2RestaurantDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RestaurantBatchUtils {

    private final ObjectMapper objectMapper;

    public RestaurantBatchUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AreaBasedList2RestaurantDto parseSearchRestaurantDto(Map<String, Object> item) {
        return AreaBasedList2RestaurantDto.builder()
            .contentId(getStr(item, "contentid"))
            .contentTypeId(getStr(item, "contenttypeid"))
            .addr1(getStr(item, "addr1"))
            .addr2(getStr(item, "addr2"))
            .areacode(getStr(item, "areacode"))
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
            .build();
    }

    public DetailIntro2RestaurantDto parseDetailIntroRestaurantDto(Map<String, Object> response) {
        Map<String, Object> body = getNestedMap(response, "response", "body");
        Map<String, Object> items = (Map<String, Object>) body.get("items");
        if (items == null || items.get("item") == null) return new DetailIntro2RestaurantDto();

        Object itemObj = items.get("item");
        Map<String, Object> item;
        if (itemObj instanceof List<?>) {
            item = ((List<Map<String, Object>>) itemObj).get(0);
        } else if (itemObj instanceof Map<?, ?>) {
            item = (Map<String, Object>) itemObj;
        } else {
            return new DetailIntro2RestaurantDto();
        }

        return DetailIntro2RestaurantDto.builder()
            .chkcreditcardfood(getStr(item, "chkcreditcardfood"))
            .discountinfofood(getStr(item, "discountinfofood"))
            .firstmenu(getStr(item, "firstmenu"))
            .infocenterfood(getStr(item, "infocenterfood"))
            .kidsfacility(getStr(item, "kidsfacility"))
            .opendatefood(getStr(item, "opendatefood"))
            .opentimefood(getStr(item, "opentimefood"))
            .packing(getStr(item, "packing"))
            .parkingfood(getStr(item, "parkingfood"))
            .reservationfood(getStr(item, "reservationfood"))
            .restdatefood(getStr(item, "restdatefood"))
            .scalefood(getStr(item, "scalefood"))
            .seat(getStr(item, "seat"))
            .smoking(getStr(item, "smoking"))
            .treatmenu(getStr(item, "treatmenu"))
            .lcnsno(getStr(item, "lcnsno"))
            .build();
    }

    public List<DetailInfo2RestaurantDto> parseDetailInfoMenuList(Map<String, Object> response) {
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

        List<DetailInfo2RestaurantDto> menuDtos = new ArrayList<>();
        for (Map<String, Object> item : list) {
            menuDtos.add(DetailInfo2RestaurantDto.builder()
                .serialnum(getStr(item, "serialnum"))
                .menuname(getStr(item, "menuname"))
                .menuprice(getStr(item, "menuprice"))
                .build());
        }
        return menuDtos;
    }

    public List<AreaBasedList2RestaurantDto> parseRestaurantList(Map<String, Object> response) {
        try {
            Map<String, Object> body = getNestedMap(response, "response", "body");
            Map<String, Object> items = (Map<String, Object>) body.get("items");

            if (items == null || items.get("item") == null) {
                return Collections.emptyList();
            }

            Object itemObj = items.get("item");
            List<Map<String, Object>> restaurantList;

            if (itemObj instanceof List<?>) {
                restaurantList = (List<Map<String, Object>>) itemObj;
            } else {
                restaurantList = List.of((Map<String, Object>) itemObj);
            }

            return restaurantList.stream()
                .map(this::parseSearchRestaurantDto)
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Failed to parse restaurant list", e);
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
                return Map.of();
            }
        }
        return current;
    }
}
