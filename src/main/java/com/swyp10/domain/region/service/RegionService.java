package com.swyp10.domain.region.service;

import com.swyp10.domain.region.entity.Region;
import com.swyp10.domain.region.repository.RegionRepository;
import com.swyp10.exception.ApplicationException;
import com.swyp10.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionRepository regionRepository;

    public Region getRegion(int regionCode) {
        return regionRepository.findById(regionCode)
            .orElseThrow(() -> new ApplicationException(ErrorCode.BAD_REQUEST, "Region not found: " + regionCode));
    }

    @Transactional
    public Region createRegion(Region region) {
        return regionRepository.save(region);
    }

    @Transactional
    public void deleteRegion(int regionCode) {
        regionRepository.deleteById(regionCode);
    }
}
