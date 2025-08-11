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

    // 기존 메서드들 (하위 호환성을 위해 유지)
    public static FestivalBasicInfo toBasicInfo(SearchFestival2Dto dto) {
        return toBasicInfoFromThreeDto(dto, null, null);
    }
    
    public static FestivalDetailIntro toDetailIntro(DetailIntro2Dto dto) {
        return toDetailIntroFromThreeDto(null, null, dto);
    }
    
    // 새로운 3개 DTO 버전 메서드들
    public static FestivalBasicInfo toBasicInfoFromThreeDto(
            SearchFestival2Dto searchDto,
            DetailCommon2Dto detailDto,
            DetailIntro2Dto introDto) {
        
        // SearchFestival2Dto를 우선으로 하되, null이면 다른 DTO에서 값을 가져옴
        String addr1 = getValue(searchDto != null ? searchDto.getAddr1() : null,
                               detailDto != null ? detailDto.getAddr1() : null,
                               null);
        
        String areacode = getValue(searchDto != null ? searchDto.getAreacode() : null,
                                  detailDto != null ? detailDto.getAreacode() : null,
                                  null);
        
        String contenttypeid = getValue(searchDto != null ? searchDto.getContenttypeid() : null,
                                       detailDto != null ? detailDto.getContenttypeid() : null,
                                       null);
        
        String createdtime = getValue(searchDto != null ? searchDto.getCreatedtime() : null,
                                     detailDto != null ? detailDto.getCreatedtime() : null,
                                     null);
        
        String eventstartdate = getValue(searchDto != null ? searchDto.getEventstartdate() : null,
                                        introDto != null ? introDto.getEventstartdate() : null,
                                        null);
        
        String eventenddate = getValue(searchDto != null ? searchDto.getEventenddate() : null,
                                      introDto != null ? introDto.getEventenddate() : null,
                                      null);
        
        String firstimage = getValue(searchDto != null ? searchDto.getFirstimage() : null,
                                    detailDto != null ? detailDto.getFirstimage() : null,
                                    null);
        
        String firstimage2 = getValue(searchDto != null ? searchDto.getFirstimage2() : null,
                                     detailDto != null ? detailDto.getFirstimage2() : null,
                                     null);
        
        String mapxStr = getValue(searchDto != null ? searchDto.getMapx() : null,
                                 detailDto != null ? detailDto.getMapx() : null,
                                 null);
        Double mapx = (mapxStr != null && !mapxStr.isBlank()) ? parseDouble(mapxStr) : null;
        
        String mapyStr = getValue(searchDto != null ? searchDto.getMapy() : null,
                                 detailDto != null ? detailDto.getMapy() : null,
                                 null);
        Double mapy = (mapyStr != null && !mapyStr.isBlank()) ? parseDouble(mapyStr) : null;
        
        String modifiedtime = getValue(searchDto != null ? searchDto.getModifiedtime() : null,
                                      detailDto != null ? detailDto.getModifiedtime() : null,
                                      null);
        
        String sigungucode = getValue(searchDto != null ? searchDto.getSigungucode() : null,
                                     detailDto != null ? detailDto.getSigungucode() : null,
                                     null);
        
        String tel = getValue(searchDto != null ? searchDto.getTel() : null,
                             detailDto != null ? detailDto.getTel() : null,
                             null);
        
        String title = getValue(searchDto != null ? searchDto.getTitle() : null,
                               detailDto != null ? detailDto.getTitle() : null,
                               null);
        
        String lDongRegnCd = getValue(searchDto != null ? searchDto.getLDongRegnCd() : null,
                                     detailDto != null ? detailDto.getLDongRegnCd() : null,
                                     null);
        
        String lDongSignguCd = getValue(searchDto != null ? searchDto.getLDongSignguCd() : null,
                                       detailDto != null ? detailDto.getLDongSignguCd() : null,
                                       null);
        
        String lclsSystm1 = getValue(searchDto != null ? searchDto.getLclsSystm1() : null,
                                    detailDto != null ? detailDto.getLclsSystm1() : null,
                                    null);
        
        String progresstype = searchDto != null ? searchDto.getProgresstype() : null;
        String festivaltype = searchDto != null ? searchDto.getFestivaltype() : null;
        
        return FestivalBasicInfo.builder()
            .addr1(addr1)
            .areacode(areacode)
            .contenttypeid(contenttypeid)
            .createdtime(createdtime)
            .eventstartdate(parseToLocalDate(eventstartdate))
            .eventenddate(parseToLocalDate(eventenddate))
            .firstimage(firstimage)
            .firstimage2(firstimage2)
            .mapx(mapx)
            .mapy(mapy)
            .modifiedtime(modifiedtime)
            .sigungucode(sigungucode)
            .tel(tel)
            .title(title)
            .lDongRegnCd(lDongRegnCd)
            .lDongSignguCd(lDongSignguCd)
            .lclsSystm1(lclsSystm1)
            .progresstype(progresstype)
            .festivaltype(festivaltype)
            .build();
    }

    public static FestivalDetailIntro toDetailIntroFromThreeDto(
            SearchFestival2Dto searchDto,
            DetailCommon2Dto detailDto,
            DetailIntro2Dto introDto) {
        
        // eventhomepage는 homepage 값을 우선적으로 사용
        String eventhomepage = getValue(detailDto != null ? detailDto.getHomepage() : null,
                                       introDto != null ? introDto.getEventhomepage() : null,
                                       null);
        
        String agelimit = introDto != null ? introDto.getAgelimit() : null;
        String bookingplace = introDto != null ? introDto.getBookingplace() : null;
        String discountinfofestival = introDto != null ? introDto.getDiscountinfofestival() : null;
        String eventplace = introDto != null ? introDto.getEventplace() : null;
        String festivalgrade = introDto != null ? introDto.getFestivalgrade() : null;
        String placeinfo = introDto != null ? introDto.getPlaceinfo() : null;
        String playtime = introDto != null ? introDto.getPlaytime() : null;
        String program = introDto != null ? introDto.getProgram() : null;
        String spendtimefestival = introDto != null ? introDto.getSpendtimefestival() : null;
        String sponsor1 = introDto != null ? introDto.getSponsor1() : null;
        String sponsor1tel = introDto != null ? introDto.getSponsor1tel() : null;
        String sponsor2 = introDto != null ? introDto.getSponsor2() : null;
        String sponsor2tel = introDto != null ? introDto.getSponsor2tel() : null;
        String subevent = introDto != null ? introDto.getSubevent() : null;
        String usetimefestival = introDto != null ? introDto.getUsetimefestival() : null;
        
        return FestivalDetailIntro.builder()
            .agelimit(agelimit)
            .bookingplace(bookingplace)
            .discountinfofestival(discountinfofestival)
            .eventhomepage(eventhomepage)  // homepage 값을 우선 사용
            .eventplace(eventplace)
            .festivalgrade(festivalgrade)
            .placeinfo(placeinfo)
            .playtime(playtime)
            .program(program)
            .spendtimefestival(spendtimefestival)
            .sponsor1(sponsor1)
            .sponsor1tel(sponsor1tel)
            .sponsor2(sponsor2)
            .sponsor2tel(sponsor2tel)
            .subevent(subevent)
            .usetimefestival(usetimefestival)
            .build();
    }

    // 3개 값 중 null이 아닌 첫 번째 값을 반환하는 헬퍼 메서드
    private static String getValue(String first, String second, String third) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        if (second != null && !second.trim().isEmpty()) {
            return second;
        }
        if (third != null && !third.trim().isEmpty()) {
            return third;
        }
        return null;
    }
    
    // Double 파싱 헬퍼 메서드
    private static Double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // overview 값을 가져오는 메서드
    public static String getOverview(
            SearchFestival2Dto searchDto,
            DetailCommon2Dto detailDto,
            DetailIntro2Dto introDto) {
        return detailDto != null ? detailDto.getOverview() : null;
    }
    
    // contentId를 가져오는 메서드
    public static String getContentId(
            SearchFestival2Dto searchDto,
            DetailCommon2Dto detailDto,
            DetailIntro2Dto introDto) {
        return getValue(searchDto != null ? searchDto.getContentid() : null,
                       detailDto != null ? detailDto.getContentid() : null,
                       introDto != null ? introDto.getContentid() : null);
    }

    public static LocalDate parseToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(dateStr, formatter);
        }catch (DateTimeParseException e) {
            return null;
        }
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

    // 새로운 Entity 생성 메서드 - 3개 DTO를 모두 받아서 처리
    public static Festival toEntity(
        SearchFestival2Dto searchFestival2Dto,
        DetailCommon2Dto detailCommon2Dto,
        DetailIntro2Dto detailIntro2Dto,
        List<DetailImage2Dto> detailImage2DtoList
    ) {
        // contentId 가져오기
        String contentId = getContentId(searchFestival2Dto, detailCommon2Dto, detailIntro2Dto);
        
        // overview 가져오기
        String overview = getOverview(searchFestival2Dto, detailCommon2Dto, detailIntro2Dto);
        
        // 날짜 정보 가져오기
        String eventstartdateStr = getValue(
            searchFestival2Dto != null ? searchFestival2Dto.getEventstartdate() : null,
            detailIntro2Dto != null ? detailIntro2Dto.getEventstartdate() : null,
            null
        );
        String eventenddateStr = getValue(
            searchFestival2Dto != null ? searchFestival2Dto.getEventenddate() : null,
            detailIntro2Dto != null ? detailIntro2Dto.getEventenddate() : null,
            null
        );
        
        LocalDate startDate = parseDate(eventstartdateStr);
        LocalDate endDate = parseDate(eventenddateStr);

        // title 가져오기
        String title = getValue(
            searchFestival2Dto != null ? searchFestival2Dto.getTitle() : null,
            detailCommon2Dto != null ? detailCommon2Dto.getTitle() : null,
            null
        );
        
        // areacode 가져오기
        String areacode = getValue(
            searchFestival2Dto != null ? searchFestival2Dto.getAreacode() : null,
            detailCommon2Dto != null ? detailCommon2Dto.getAreacode() : null,
            null
        );

        FestivalBasicInfo basicInfo = toBasicInfoFromThreeDto(searchFestival2Dto, detailCommon2Dto, detailIntro2Dto);
        FestivalDetailIntro detailIntro = toDetailIntroFromThreeDto(searchFestival2Dto, detailCommon2Dto, detailIntro2Dto);
        List<FestivalImage> images = detailImage2DtoList != null ? 
            detailImage2DtoList.stream()
                .map(FestivalMapper::toFestivalImage)
                .collect(Collectors.toList()) : List.of();

        Festival festival = Festival.builder()
            .contentId(contentId)
            .basicInfo(basicInfo)
            .overview(overview)
            .detailIntro(detailIntro)
            .personalityType(mapPersonalTypeByName(title))
            .status(calculateStatus(startDate, endDate))
            .theme(mapThemeByText(title, overview))
            .withWhom(mapWithWhomByText(title, overview))
            .regionFilter(mapRegionFilterByAreaCode(areacode))
            .detailImages(images)
            .build();

        images.forEach(img -> img.setFestival(festival));
        return festival;
    }
}