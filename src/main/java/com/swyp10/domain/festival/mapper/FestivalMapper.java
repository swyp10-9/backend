package com.swyp10.domain.festival.mapper;

import com.swyp10.domain.festival.dto.tourapi.*;
import com.swyp10.domain.festival.entity.*;
import com.swyp10.domain.festival.enums.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class FestivalMapper {

    public static FestivalBasicInfo toBasicInfo(SearchFestival2Dto dto) {
        if(dto == null) return null;

        String mapxStr = dto.getMapx();
        Double mapx = (mapxStr != null && !mapxStr.isBlank()) ? Double.parseDouble(mapxStr) : null;

        String mapyStr = dto.getMapy();
        Double mapy = (mapyStr != null && !mapyStr.isBlank()) ? Double.parseDouble(mapyStr) : null;

        return FestivalBasicInfo.builder()
            .addr1(dto.getAddr1())
            .areacode(dto.getAreacode())
            .contenttypeid(dto.getContenttypeid())
            .createdtime(dto.getCreatedtime())
            .eventstartdate(parseToLocalDate(dto.getEventstartdate()))
            .eventenddate(parseToLocalDate(dto.getEventenddate()))
            .firstimage(dto.getFirstimage())
            .firstimage2(dto.getFirstimage2())
            .mapx(mapx)
            .mapy(mapy)
            .modifiedtime(dto.getModifiedtime())
            .sigungucode(dto.getSigungucode())
            .tel(dto.getTel())
            .title(dto.getTitle())
            .lDongRegnCd(dto.getLDongRegnCd())
            .lDongSignguCd(dto.getLDongSignguCd())
            .lclsSystm1(dto.getLclsSystm1())
            .progresstype(dto.getProgresstype())
            .festivaltype(dto.getFestivaltype())
            .build();
    }

    public static LocalDate parseToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(dateStr, formatter);
    }

    public static FestivalDetailIntro toDetailIntro(DetailIntro2Dto dto) {
        if(dto == null) return null;
        return FestivalDetailIntro.builder()
            .agelimit(dto.getAgelimit())
            .bookingplace(dto.getBookingplace())
            .discountinfofestival(dto.getDiscountinfofestival())
            .eventhomepage(dto.getEventhomepage())
            .eventplace(dto.getEventplace())
            .festivalgrade(dto.getFestivalgrade())
            .placeinfo(dto.getPlaceinfo())
            .playtime(dto.getPlaytime())
            .program(dto.getProgram())
            .spendtimefestival(dto.getSpendtimefestival())
            .sponsor1(dto.getSponsor1())
            .sponsor1tel(dto.getSponsor1tel())
            .sponsor2(dto.getSponsor2())
            .sponsor2tel(dto.getSponsor2tel())
            .subevent(dto.getSubevent())
            .usetimefestival(dto.getUsetimefestival())
            .build();
    }

    public static FestivalImage toFestivalImage(DetailImage2Dto dto) {
        if(dto == null) return null;
        return FestivalImage.builder()
            .imgid(dto.getImgid())
            .originimgurl(dto.getOriginimgurl())
            .smallimageurl(dto.getSmallimageurl())
            .serialnum(dto.getSerialnum())
            .build();
    }

    public static FestivalPersonalityType mapPersonalTypeByName(String festivalName) {
        String nameLower = festivalName.toLowerCase();

        if (nameLower.contains("에너지") || nameLower.contains("파티")) {
            return FestivalPersonalityType.ENERGIZER;
        }
        if (nameLower.contains("탐험") || nameLower.contains("문화")) {
            return FestivalPersonalityType.EXPLORER;
        }
        if (nameLower.contains("큐레이터") || nameLower.contains("전시")) {
            return FestivalPersonalityType.CURATOR;
        }
        if (nameLower.contains("소셜") || nameLower.contains("친구")) {
            return FestivalPersonalityType.SOCIALIZER;
        }
        if (nameLower.contains("힐링") || nameLower.contains("휴식")) {
            return FestivalPersonalityType.HEALER;
        }

        return FestivalPersonalityType.SOCIALIZER;
    }

    public static FestivalStatus calculateStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (startDate == null || endDate == null) {
            return FestivalStatus.ALL;
        }

        if (!today.isBefore(startDate) && !today.isAfter(endDate)) {
            return FestivalStatus.ONGOING;
        } else if (today.isBefore(startDate)) {
            return FestivalStatus.UPCOMING;
        } else {
            return FestivalStatus.ENDED;
        }
    }

    public static FestivalTheme mapThemeByText(String name, String overview) {
        String text = ((name == null ? "" : name) + " " + (overview == null ? "" : overview)).toLowerCase();

        if (text.contains("음식") || text.contains("미식") || text.contains("맛집") || text.contains("요리")) {
            return FestivalTheme.FOOD;
        }
        if (text.contains("음악") || text.contains("공연") || text.contains("콘서트") || text.contains("페스티벌")) {
            return FestivalTheme.MUSIC;
        }
        if (text.contains("문화") || text.contains("예술") || text.contains("전시") || text.contains("미술")) {
            return FestivalTheme.CULTURE_ART;
        }
        if (text.contains("자연") || text.contains("체험") || text.contains("산") || text.contains("숲") || text.contains("생태")) {
            return FestivalTheme.NATURE;
        }
        if (text.contains("전통") || text.contains("역사") || text.contains("민속") || text.contains("문화재")) {
            return FestivalTheme.TRADITION;
        }

        return FestivalTheme.ALL;
    }

    public static FestivalWithWhom mapWithWhomByText(String name, String overview) {
        String text = ((name == null ? "" : name) + " " + (overview == null ? "" : overview)).toLowerCase();

        if (text.contains("가족") || text.contains("아이") || text.contains("어린이")) {
            return FestivalWithWhom.FAMILY;
        }
        if (text.contains("커플") || text.contains("연인") || text.contains("데이트")) {
            return FestivalWithWhom.COUPLE;
        }
        if (text.contains("부모") || text.contains("어버이") || text.contains("어머니") || text.contains("아버지")) {
            return FestivalWithWhom.PARENTS;
        }
        if (text.contains("반려견") || text.contains("애견") || text.contains("강아지") || text.contains("반려동물")) {
            return FestivalWithWhom.PET;
        }
        if (text.contains("친구") || text.contains("우정") || text.contains("친구와")) {
            return FestivalWithWhom.FRIENDS;
        }
        return FestivalWithWhom.ALL;  // 기본값
    }

    public static RegionFilter mapRegionFilterByAreaCode(String areaCode) {
        if (areaCode == null) {
            return RegionFilter.ALL; // 기본값
        }
        switch (areaCode) {
            case "1":  // 서울
                return RegionFilter.SEOUL;
            case "2":  // 인천 (경기)
            case "31": // 경기도
                return RegionFilter.GYEONGGI;
            case "3":  // 대전 (충청)
            case "8":  // 세종특별자치시 (충청)
            case "11": // 충청북도
            case "12": // 충청남도
            case "34": // 충청남도
                return RegionFilter.CHUNGCHEONG;
            case "4":  // 대구 (경상)
            case "6":  // 부산 (경상)
            case "7":  // 울산 (경상)
            case "13": // 경상북도
            case "14": // 경상남도
            case "36": // 경상남도
                return RegionFilter.GYEONGSANG;
            case "5":  // 광주 (전라)
            case "15": // 전북특별자치도
            case "16": // 전라남도
            case "38": // 전라남도
                return RegionFilter.JEOLLA;
            case "32": // 강원특별자치도
                return RegionFilter.GANGWON;
            case "39": // 제주도
                return RegionFilter.JEJU;
            default:
                return RegionFilter.ALL;  // 미분류 또는 전체
        }
    }

    private static LocalDate parseDate(String yyyymmdd) {
        if (yyyymmdd == null || yyyymmdd.length() != 8) return null;
        try {
            return LocalDate.parse(yyyymmdd, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static Festival toEntity(
        SearchFestival2Dto searchFestival2Dto,
        DetailCommon2Dto detailCommon2Dto,
        DetailIntro2Dto detailIntro2Dto,
        List<DetailImage2Dto> detailImage2DtoList
    ) {
        LocalDate startDate = parseDate(searchFestival2Dto.getEventstartdate());
        LocalDate endDate = parseDate(searchFestival2Dto.getEventenddate());

        FestivalBasicInfo basicInfo = toBasicInfo(searchFestival2Dto);
        FestivalDetailIntro detailIntro = toDetailIntro(detailIntro2Dto);
        List<FestivalImage> images = detailImage2DtoList.stream()
            .map(FestivalMapper::toFestivalImage)
            .collect(Collectors.toList());

        Festival festival = Festival.builder()
            .contentId(searchFestival2Dto.getContentid())
            .basicInfo(basicInfo)
            .overview(detailCommon2Dto.getOverview())
            .detailIntro(detailIntro)
            .personalityType(mapPersonalTypeByName(searchFestival2Dto.getTitle()))
            .status(calculateStatus(startDate, endDate))
            .theme(mapThemeByText(searchFestival2Dto.getTitle(), detailCommon2Dto.getOverview()))
            .withWhom(mapWithWhomByText(searchFestival2Dto.getTitle(), detailCommon2Dto.getOverview()))
            .regionFilter(mapRegionFilterByAreaCode(searchFestival2Dto.getAreacode()))
            .detailImages(images)
            .build();

        images.forEach(img -> img.setFestival(festival));
        return festival;
    }
}
