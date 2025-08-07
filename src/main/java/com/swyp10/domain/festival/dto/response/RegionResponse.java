package com.swyp10.domain.festival.dto.response;

import com.swyp10.domain.region.entity.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionResponse {
    
    @Schema(description = "지역 코드", required = true, nullable = false, example = "1")
    private Integer regionCode;
    
    @Schema(description = "지역명", required = true, nullable = false, example = "서울특별시")
    private String regionName;
    
    @Schema(description = "상위 지역 코드", required = false, nullable = true, example = "11")
    private String parentCode;
    
    public static RegionResponse from(Region region) {
        return RegionResponse.builder()
                .regionCode(region.getRegionCode())
                .regionName(region.getRegionName())
                .parentCode(region.getParentCode())
                .build();
    }
}
