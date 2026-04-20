package com.shop.shop.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.shop.dto.*;
import com.shop.shop.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getOrders(
            @RequestAttribute Long userId,
            @RequestAttribute String userRole,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getOrders(userId, userRole, page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(
            @PathVariable Long id,
            @RequestAttribute Long userId,
            @RequestAttribute String userRole) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrder(id, userId, userRole)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(request, userId)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @RequestAttribute Long userId,
            @RequestAttribute String userRole) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(id, request.getStatus(), userId)));
    }

    @PutMapping("/{id}/express")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateExpress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpressRequest request,
            @RequestAttribute Long userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateExpress(id, request, userId)));
    }

    @GetMapping("/{id}/waybill")
    public ResponseEntity<ApiResponse<WaybillDTO>> getWaybill(
            @PathVariable Long id,
            @RequestAttribute Long userId,
            @RequestAttribute String userRole) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getWaybill(id, userId, userRole)));
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<ApiResponse<List<RouteDTO>>> getTrackingRoutes(
            @PathVariable Long id,
            @RequestAttribute Long userId,
            @RequestAttribute String userRole) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getTrackingRoutes(id, userId, userRole)));
    }

    @PostMapping("/{id}/cancel-logistics")
    @PreAuthorize("hasAuthority('ROLE_MERCHANT')")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelLogistics(
            @PathVariable Long id,
            @RequestAttribute Long userId,
            @RequestAttribute String userRole) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelLogistics(id, userId, userRole)));
    }
}
