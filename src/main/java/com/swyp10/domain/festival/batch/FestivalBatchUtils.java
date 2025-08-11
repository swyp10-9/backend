package com.swyp10.domain.festival.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swyp10.domain.festival.dto.tourapi.DetailCommon2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailImage2Dto;
import com.swyp10.domain.festival.dto.tourapi.DetailIntro2Dto;
import com.swyp10.domain.festival.dto.tourapi.SearchFestival2Dto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class FestivalBatchUtils {

    private final ObjectMapper objectMapper;

    public String extractUrlFromAnchor(String html) {
        if (html == null) return null;
        Pattern pattern = Pattern.compile("href=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);  // 첫 번째 그룹, 즉 href 안의 URL
        }
        return null;
    }

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
                .contentid(getStr(firstItem, "contentid"))
                .sigungucode(getStr(firstItem, "sigungucode"))
                .cat1(getStr(firstItem, "cat1"))
                .cat2(getStr(firstItem, "cat2"))
                .cat3(getStr(firstItem, "cat3"))
                .addr1(getStr(firstItem, "addr1"))
                .addr2(getStr(firstItem, "addr2"))
                .zipcode(getStr(firstItem, "zipcode"))
                .mapx(getStr(firstItem, "mapx"))
                .mapy(getStr(firstItem, "mapy"))
                .mlevel(getStr(firstItem, "mlevel"))
                .cpyrhtDivCd(getStr(firstItem, "cpyrhtDivCd"))
                .contenttypeid(getStr(firstItem, "contenttypeid"))
                .createdtime(getStr(firstItem, "createdtime"))
                .homepage(extractUrlFromAnchor(getStr(firstItem, "homepage")))
                .modifiedtime(getStr(firstItem, "modifiedtime"))
                .tel(getStr(firstItem, "tel"))
                .telname(getStr(firstItem, "telname"))
                .title(getStr(firstItem, "title"))
                .firstimage(getStr(firstItem, "firstimage"))
                .firstimage2(getStr(firstItem, "firstimage2"))
                .areacode(getStr(firstItem, "areacode"))
                .lDongRegnCd(getStr(firstItem, "lDongRegnCd"))
                .lDongSignguCd(getStr(firstItem, "lDongSignguCd"))
                .lclsSystm1(getStr(firstItem, "lclsSystm1"))
                .lclsSystm2(getStr(firstItem, "lclsSystm2"))
                .lclsSystm3(getStr(firstItem, "lclsSystm3"))
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
                .chkpetculture(getStr(firstItem, "chkpetculture"))
                .eventhomepage(getStr(firstItem, "eventhomepage"))
                .eventplace(getStr(firstItem, "eventplace"))
                .parkingleports(getStr(firstItem, "parkingleports"))
                .reservation(getStr(firstItem, "reservation"))
                .restdateleports(getStr(firstItem, "restdateleports"))
                .eventstartdate(getStr(firstItem, "eventstartdate"))
                .festivalgrade(getStr(firstItem, "festivalgrade"))
                .karaoke(getStr(firstItem, "karaoke"))
                .discountinfofood(getStr(firstItem, "discountinfofood"))
                .firstmenu(getStr(firstItem, "firstmenu"))
                .infocenterfood(getStr(firstItem, "infocenterfood"))
                .kidsfacility(getStr(firstItem, "kidsfacility"))
                .opendatefood(getStr(firstItem, "opendatefood"))
                .opentimefood(getStr(firstItem, "opentimefood"))
                .packing(getStr(firstItem, "packing"))
                .parkingfood(getStr(firstItem, "parkingfood"))
                .reservationfood(getStr(firstItem, "reservationfood"))
                .chkcreditcardculture(getStr(firstItem, "chkcreditcardculture"))
                .scaleleports(getStr(firstItem, "scaleleports"))
                .usefeeleports(getStr(firstItem, "usefeeleports"))
                .discountinfofestival(getStr(firstItem, "discountinfofestival"))
                .chkcreditcardfood(getStr(firstItem, "chkcreditcardfood"))
                .eventenddate(getStr(firstItem, "eventenddate"))
                .playtime(getStr(firstItem, "playtime"))
                .chkbabycarriageculture(getStr(firstItem, "chkbabycarriageculture"))
                .roomcount(getStr(firstItem, "roomcount"))
                .reservationlodging(getStr(firstItem, "reservationlodging"))
                .reservationurl(getStr(firstItem, "reservationurl"))
                .roomtype(getStr(firstItem, "roomtype"))
                .scalelodging(getStr(firstItem, "scalelodging"))
                .subfacility(getStr(firstItem, "subfacility"))
                .barbecue(getStr(firstItem, "barbecue"))
                .beauty(getStr(firstItem, "beauty"))
                .beverage(getStr(firstItem, "beverage"))
                .bicycle(getStr(firstItem, "bicycle"))
                .campfire(getStr(firstItem, "campfire"))
                .fitness(getStr(firstItem, "fitness"))
                .placeinfo(getStr(firstItem, "placeinfo"))
                .parkinglodging(getStr(firstItem, "parkinglodging"))
                .pickup(getStr(firstItem, "pickup"))
                .publicbath(getStr(firstItem, "publicbath"))
                .opendate(getStr(firstItem, "opendate"))
                .parking(getStr(firstItem, "parking"))
                .restdate(getStr(firstItem, "restdate"))
                .usetimeleports(getStr(firstItem, "usetimeleports"))
                .foodplace(getStr(firstItem, "foodplace"))
                .infocenterlodging(getStr(firstItem, "infocenterlodging"))
                .restdatefood(getStr(firstItem, "restdatefood"))
                .scalefood(getStr(firstItem, "scalefood"))
                .seat(getStr(firstItem, "seat"))
                .smoking(getStr(firstItem, "smoking"))
                .treatmenu(getStr(firstItem, "treatmenu"))
                .lcnsno(getStr(firstItem, "lcnsno"))
                .contentid(getStr(firstItem, "contentid"))
                .contenttypeid(getStr(firstItem, "contenttypeid"))
                .accomcount(getStr(firstItem, "accomcount"))
                .chkbabycarriage(getStr(firstItem, "chkbabycarriage"))
                .chkcreditcard(getStr(firstItem, "chkcreditcard"))
                .chkpet(getStr(firstItem, "chkpet"))
                .expagerange(getStr(firstItem, "expagerange"))
                .expguide(getStr(firstItem, "expguide"))
                .heritage1(getStr(firstItem, "heritage1"))
                .heritage2(getStr(firstItem, "heritage2"))
                .heritage3(getStr(firstItem, "heritage3"))
                .infocenter(getStr(firstItem, "infocenter"))
                .taketime(getStr(firstItem, "taketime"))
                .theme(getStr(firstItem, "theme"))
                .accomcountleports(getStr(firstItem, "accomcountleports"))
                .chkbabycarriageleports(getStr(firstItem, "chkbabycarriageleports"))
                .chkcreditcardleports(getStr(firstItem, "chkcreditcardleports"))
                .chkpetleports(getStr(firstItem, "chkpetleports"))
                .expagerangeleports(getStr(firstItem, "expagerangeleports"))
                .infocenterleports(getStr(firstItem, "infocenterleports"))
                .openperiod(getStr(firstItem, "openperiod"))
                .parkingfeeleports(getStr(firstItem, "parkingfeeleports"))
                .program(getStr(firstItem, "program"))
                .spendtimefestival(getStr(firstItem, "spendtimefestival"))
                .sponsor1(getStr(firstItem, "sponsor1"))
                .sponsor1tel(getStr(firstItem, "sponsor1tel"))
                .discountinfo(getStr(firstItem, "discountinfo"))
                .infocenterculture(getStr(firstItem, "infocenterculture"))
                .parkingculture(getStr(firstItem, "parkingculture"))
                .parkingfee(getStr(firstItem, "parkingfee"))
                .restdateculture(getStr(firstItem, "restdateculture"))
                .usefee(getStr(firstItem, "usefee"))
                .usetimeculture(getStr(firstItem, "usetimeculture"))
                .scale(getStr(firstItem, "scale"))
                .spendtime(getStr(firstItem, "spendtime"))
                .agelimit(getStr(firstItem, "agelimit"))
                .bookingplace(getStr(firstItem, "bookingplace"))
                .useseason(getStr(firstItem, "useseason"))
                .usetime(getStr(firstItem, "usetime"))
                .accomcountculture(getStr(firstItem, "accomcountculture"))
                .sponsor2(getStr(firstItem, "sponsor2"))
                .sponsor2tel(getStr(firstItem, "sponsor2tel"))
                .subevent(getStr(firstItem, "subevent"))
                .usetimefestival(getStr(firstItem, "usetimefestival"))
                .distance(getStr(firstItem, "distance"))
                .infocentertourcourse(getStr(firstItem, "infocentertourcourse"))
                .schedule(getStr(firstItem, "schedule"))
                .publicpc(getStr(firstItem, "publicpc"))
                .sauna(getStr(firstItem, "sauna"))
                .seminar(getStr(firstItem, "seminar"))
                .sports(getStr(firstItem, "sports"))
                .refundregulation(getStr(firstItem, "refundregulation"))
                .chkbabycarriageshopping(getStr(firstItem, "chkbabycarriageshopping"))
                .chkcreditcardshopping(getStr(firstItem, "chkcreditcardshopping"))
                .chkpetshopping(getStr(firstItem, "chkpetshopping"))
                .culturecenter(getStr(firstItem, "culturecenter"))
                .fairday(getStr(firstItem, "fairday"))
                .infocentershopping(getStr(firstItem, "infocentershopping"))
                .opendateshopping(getStr(firstItem, "opendateshopping"))
                .opentime(getStr(firstItem, "opentime"))
                .parkingshopping(getStr(firstItem, "parkingshopping"))
                .restdateshopping(getStr(firstItem, "restdateshopping"))
                .restroom(getStr(firstItem, "restroom"))
                .saleitem(getStr(firstItem, "saleitem"))
                .saleitemcost(getStr(firstItem, "saleitemcost"))
                .scaleshopping(getStr(firstItem, "scaleshopping"))
                .shopguide(getStr(firstItem, "shopguide"))
                .checkintime(getStr(firstItem, "checkintime"))
                .checkouttime(getStr(firstItem, "checkouttime"))
                .chkcooking(getStr(firstItem, "chkcooking"))
                .accomcountlodging(getStr(firstItem, "accomcountlodging"))
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
