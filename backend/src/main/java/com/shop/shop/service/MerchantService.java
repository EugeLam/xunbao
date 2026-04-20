package com.shop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.shop.dto.MerchantDTO;
import com.shop.shop.exception.BusinessException;
import com.shop.shop.exception.ErrorCode;
import com.shop.shop.mapper.MerchantMapper;
import com.shop.shop.model.Merchant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantMapper merchantMapper;

    public MerchantDTO getMerchant(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        if (merchant == null) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return toDTO(merchant);
    }

    public MerchantDTO getMerchantByUserId(Long userId) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
        if (merchant == null) {
            throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return toDTO(merchant);
    }

    private MerchantDTO toDTO(Merchant merchant) {
        return MerchantDTO.builder()
                .id(merchant.getId())
                .userId(merchant.getUserId())
                .name(merchant.getName())
                .rating(merchant.getRating())
                .totalSales(merchant.getTotalSales())
                .build();
    }
}
