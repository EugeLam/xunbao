package com.shop.shop.controller;

import com.shop.shop.dto.*;
import com.shop.shop.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MerchantDTO>> getMerchant(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(merchantService.getMerchant(id)));
    }
}
