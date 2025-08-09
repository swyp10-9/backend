package com.swyp10.domain.restaurant.service;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.domain.festival.entity.FestivalBasicInfo;
import com.swyp10.domain.festival.repository.FestivalRepository;
import com.swyp10.domain.restaurant.dto.request.FestivalRestaurantPageRequest;
import com.swyp10.domain.restaurant.dto.response.FestivalRestaurantListResponse;
import com.swyp10.domain.restaurant.dto.response.FestivalRestaurantResponse;
import com.swyp10.domain.restaurant.dto.tourapi.AreaBasedList2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailInfo2RestaurantDto;
import com.swyp10.domain.restaurant.dto.tourapi.DetailIntro2RestaurantDto;
import com.swyp10.domain.restaurant.entity.Restaurant;
import com.swyp10.domain.restaurant.mapper.RestaurantMapper;
import com.swyp10.domain.restaurant.repository.RestaurantRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

    private final FestivalRepository festivalRepository;
    private final RestaurantRepository restaurantRepository;

    @Transactional
    public Restaurant saveOrUpdateRestaurant(AreaBasedList2RestaurantDto restaurantDto,
                                             DetailIntro2RestaurantDto introDto,
                                             List<DetailInfo2RestaurantDto> menuDtoList) {
        return restaurantRepository.findByContentId(restaurantDto.getContentId())
            .map(existing -> updateExistingRestaurant(existing, restaurantDto, introDto, menuDtoList))
            .orElseGet(() -> createNewRestaurant(restaurantDto, introDto, menuDtoList));
    }

    private Restaurant updateExistingRestaurant(Restaurant existing,
                                                AreaBasedList2RestaurantDto restaurantDto,
                                                DetailIntro2RestaurantDto introDto,
                                                List<DetailInfo2RestaurantDto> menuDtoList) {
        // 기본정보 업데이트
        existing.updateBasicInfo(RestaurantMapper.toBasicInfo(restaurantDto));

        // 상세정보 업데이트
        existing.updateDetailInfo(RestaurantMapper.toDetailInfo(introDto));

        // 메뉴 업데이트
        existing.clearMenus();
        if (menuDtoList != null) {
            menuDtoList.stream()
                .map(RestaurantMapper::toMenu)
                .forEach(existing::addMenu);
        }
        return existing;
    }

    @Transactional
    public Restaurant createNewRestaurant(AreaBasedList2RestaurantDto restaurantDto,
                                           DetailIntro2RestaurantDto introDto,
                                           List<DetailInfo2RestaurantDto> menuDtoList) {
        Restaurant restaurant = RestaurantMapper.toEntity(restaurantDto, introDto, menuDtoList);
        restaurantRepository.save(restaurant);
        return restaurant;
    }

    @Transactional(readOnly = true)
    public Restaurant findByContentId(String contentId) {
        return restaurantRepository.findByContentId(contentId)
            .orElseThrow(() -> new ApplicationException(ErrorCode.RESTAURANT_NOT_FOUND));
    }

    @Transactional
    public void deleteRestaurant(Long restaurantId) {
        restaurantRepository.deleteById(restaurantId);
    }

    public FestivalRestaurantListResponse getFestivalRestaurants(FestivalRestaurantPageRequest request) {
        // 1) 축제 로드
        Festival festival = festivalRepository.findById(request.getFestivalId())
            .orElseThrow(() -> new ApplicationException(ErrorCode.FESTIVAL_NOT_FOUND,
                "축제를 찾을 수 없습니다. id=" + request.getFestivalId()));

        FestivalBasicInfo basic = festival.getBasicInfo();
        String areaCode = (basic != null) ? basic.getAreacode() : null;
        Double centerLat = (basic != null) ? basic.getMapy() : null;
        Double centerLng = (basic != null) ? basic.getMapx() : null;

        // 2) 페이지/정렬
        PageRequest pageable = PageRequest.of(request.getPage(), request.getSize());

        // 3) 레스토랑 조회
        Page<Restaurant> page = restaurantRepository.findByAreaWithFilters(
            areaCode,
            request.getCategory(),
            request.getRadius(),
            centerLat,
            centerLng,
            request.getSort(),
            pageable
        );

        // 4) DTO 매핑 + PageResponse 조립
        return FestivalRestaurantListResponse.builder()
            .content(page.map(this::toDto).getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .empty(page.isEmpty())
            .build();
    }

    private FestivalRestaurantResponse toDto(Restaurant restaurant) {
        return FestivalRestaurantResponse.builder()
            .name(restaurant.getBasicInfo().getTitle())
            .address(restaurant.getBasicInfo().getAddr1())
            .imageUrl(restaurant.getBasicInfo().getFirstimage())
            .build();
    }
}
