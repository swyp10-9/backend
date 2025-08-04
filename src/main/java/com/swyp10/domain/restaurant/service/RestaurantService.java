package com.swyp10.domain.restaurant.service;

import com.swyp10.domain.restaurant.dto.request.FestivalRestaurantPageRequest;
import com.swyp10.domain.restaurant.dto.response.FestivalRestaurantListResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestaurantService {

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
        return null;
    }
}
