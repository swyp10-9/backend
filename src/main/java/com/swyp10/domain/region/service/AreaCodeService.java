package com.swyp10.domain.region.service;

import com.swyp10.domain.region.entity.AreaCode;
import com.swyp10.domain.region.repository.AreaCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AreaCodeService {

    private final AreaCodeRepository areaCodeRepository;

    public List<AreaCode> findAll() {
        return areaCodeRepository.findAll();
    }

    public void saveAll(List<AreaCode> areaCodes) {
        areaCodeRepository.saveAll(areaCodes);
    }

    public void deleteAll() {
        areaCodeRepository.deleteAll();
    }
}
