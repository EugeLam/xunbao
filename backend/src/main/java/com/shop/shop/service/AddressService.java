package com.shop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shop.shop.dto.AddressDTO;
import com.shop.shop.dto.CreateAddressRequest;
import com.shop.shop.exception.BusinessException;
import com.shop.shop.exception.ErrorCode;
import com.shop.shop.mapper.AddressMapper;
import com.shop.shop.mapper.UserMapper;
import com.shop.shop.model.Address;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;
    private final UserMapper userMapper;

    public List<AddressDTO> getAddresses(Long userId) {
        return addressMapper.selectList(
                        new LambdaQueryWrapper<Address>()
                                .eq(Address::getUserId, userId)
                                .orderByDesc(Address::getIsDefault))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO createAddress(CreateAddressRequest request, Long userId) {
        if (userMapper.selectById(userId) == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultForUser(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .receiverName(request.getReceiverName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .city(request.getCity())
                .district(request.getDistrict())
                .detailAddress(request.getDetailAddress())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();

        addressMapper.insert(address);
        return toDTO(address);
    }

    @Transactional
    public AddressDTO updateAddress(Long id, CreateAddressRequest request, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultForUser(userId);
        }

        address.setReceiverName(request.getReceiverName());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
        if (request.getIsDefault() != null) {
            address.setIsDefault(request.getIsDefault());
        }

        addressMapper.updateById(address);
        return toDTO(address);
    }

    @Transactional
    public void deleteAddress(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        addressMapper.deleteById(id);
    }

    @Transactional
    public AddressDTO setDefault(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        clearDefaultForUser(userId);
        address.setIsDefault(true);
        addressMapper.updateById(address);

        return toDTO(address);
    }

    private void clearDefaultForUser(Long userId) {
        Address address = new Address();
        address.setIsDefault(false);
        LambdaUpdateWrapper<Address> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        addressMapper.update(address, wrapper);
    }

    private AddressDTO toDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .receiverName(address.getReceiverName())
                .phone(address.getPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .isDefault(address.getIsDefault())
                .build();
    }
}
