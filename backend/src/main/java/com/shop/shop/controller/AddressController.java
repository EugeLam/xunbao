package com.shop.shop.controller;

import com.shop.shop.dto.*;
import com.shop.shop.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddresses(@RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(addressService.getAddresses(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressDTO>> createAddress(
            @Valid @RequestBody CreateAddressRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(addressService.createAddress(request, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody CreateAddressRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(addressService.updateAddress(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        addressService.deleteAddress(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressDTO>> setDefault(
            @PathVariable Long id,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(addressService.setDefault(id, userId)));
    }
}
