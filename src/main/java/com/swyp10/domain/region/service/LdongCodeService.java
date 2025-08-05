package com.swyp10.domain.region.service;

import com.swyp10.domain.region.entity.LdongCode;
import com.swyp10.domain.region.repository.LdongCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LdongCodeService {

    private final LdongCodeRepository ldongCodeRepository;

    public List<LdongCode> findAll() {
        return ldongCodeRepository.findAll();
    }

    public void saveAll(List<LdongCode> ldongCodes) {
        ldongCodeRepository.saveAll(ldongCodes);
    }

    public void deleteAll() {
        ldongCodeRepository.deleteAll();
    }
}
