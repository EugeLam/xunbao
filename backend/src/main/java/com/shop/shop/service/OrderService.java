package com.shop.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.shop.dto.AddressDTO;
import com.shop.shop.dto.RouteDTO;
import com.shop.shop.dto.CreateOrderRequest;
import com.shop.shop.dto.OrderDTO;
import com.shop.shop.dto.UpdateExpressRequest;
import com.shop.shop.dto.WaybillDTO;
import com.shop.shop.dto.WaybillRequest;
import com.shop.shop.exception.BusinessException;
import com.shop.shop.exception.ErrorCode;
import com.shop.shop.mapper.*;
import com.shop.shop.model.*;
import com.shop.shop.service.logistics.LogisticsFactory;
import com.shop.shop.service.logistics.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductVariantMapper variantMapper;
    private final AddressMapper addressMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final CartItemMapper cartItemMapper;
    private final LogisticsFactory logisticsFactory;

    public Page<OrderDTO> getOrders(Long userId, String role, int page, int size) {
        if ("MERCHANT".equals(role)) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
            if (merchant == null) {
                throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
            }
            List<Product> merchantProducts = productMapper.selectList(
                    new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchant.getId()));
            List<Long> productIds = merchantProducts.stream().map(Product::getId).collect(Collectors.toList());

            IPage<Order> orderPage = orderMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Order>());
            List<OrderDTO> merchantOrders = orderPage.getRecords().stream()
                    .filter(order -> {
                        List<OrderItem> items = orderItemMapper.selectList(
                                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
                        return items.stream().anyMatch(item -> productIds.contains(item.getProductId()));
                    })
                    .map(this::toDTO)
                    .collect(Collectors.toList());

            Page<OrderDTO> resultPage1 = new Page<>(page, size);
            resultPage1.setRecords(merchantOrders);
            resultPage1.setTotal(orderPage.getTotal());
            return resultPage1;
        } else {
            IPage<Order> orderPage = orderMapper.selectPage(
                    Page.of(page, size),
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getBuyerId, userId)
                            .orderByDesc(Order::getCreatedAt));

            List<OrderDTO> dtos = orderPage.getRecords().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            Page<OrderDTO> resultPage2 = new Page<>(page, size);
            resultPage2.setRecords(dtos);
            resultPage2.setTotal(orderPage.getTotal());
            return resultPage2;
        }
    }

    public OrderDTO getOrder(Long id, Long userId, String role) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if ("MERCHANT".equals(role)) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
            if (merchant == null) {
                throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
            }
            List<Product> merchantProducts = productMapper.selectList(
                    new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchant.getId()));
            List<Long> merchantProductIds = merchantProducts.stream().map(Product::getId).collect(Collectors.toList());

            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

            boolean hasAccess = orderItems.stream().anyMatch(item -> merchantProductIds.contains(item.getProductId()));
            if (!hasAccess) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        } else if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return toDTO(order);
    }

    @Transactional
    public OrderDTO createOrder(CreateOrderRequest request, Long userId) {
        User buyer = userMapper.selectById(userId);
        if (buyer == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        Address address = addressMapper.selectById(request.getAddressId());
        if (address == null) {
            throw new BusinessException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Product product = productMapper.selectById(itemRequest.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "ID: " + itemRequest.getProductId());
            }

            ProductVariant variant = null;
            BigDecimal price = product.getPrice();

            if (itemRequest.getVariantId() != null) {
                variant = variantMapper.selectById(itemRequest.getVariantId());
                if (variant == null) {
                    throw new BusinessException(ErrorCode.VARIANT_NOT_FOUND);
                }
                price = variant.getPrice();

                if (variant.getStock() < itemRequest.getQuantity()) {
                    throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, variant.getSku());
                }
                variant.setStock(variant.getStock() - itemRequest.getQuantity());
                variantMapper.updateById(variant);
            } else {
                if (product.getStock() < itemRequest.getQuantity()) {
                    throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, product.getName());
                }
                product.setStock(product.getStock() - itemRequest.getQuantity());
                product.setSales((product.getSales() == null ? 0 : product.getSales()) + itemRequest.getQuantity());
                productMapper.updateById(product);
            }

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .variantId(itemRequest.getVariantId())
                    .quantity(itemRequest.getQuantity())
                    .price(price)
                    .build();

            orderItems.add(item);
        }

        Order order = Order.builder()
                .buyerId(userId)
                .addressId(address.getId())
                .totalAmount(totalAmount)
                .status("PENDING")
                .build();

        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));

        return toDTO(order);
    }

    @Transactional
    public OrderDTO updateStatus(Long id, String status, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        String newStatus;
        try {
            newStatus = status.toUpperCase();
            switch (newStatus) {
                case "PENDING", "PAID", "SHIPPED", "COMPLETED", "CANCELLED" -> {}
                default -> throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_STATUS, status);
        }

        order.setStatus(newStatus);
        orderMapper.updateById(order);

        return toDTO(order);
    }

    @Transactional
    public OrderDTO updateExpress(Long id, UpdateExpressRequest request, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        String trackingNumber = request.getTrackingNumber();
        LogisticsService logistics = logisticsFactory.getAvailableService(request.getExpressCompany());

        if (logistics != null) {
            Address address = addressMapper.selectById(order.getAddressId());
            WaybillRequest waybillRequest = WaybillRequest.builder()
                    .orderId("ORDER_" + order.getId())
                    .senderName("寻宝商城")
                    .senderPhone("4001234567")
                    .senderProvince("广东")
                    .senderCity("深圳")
                    .senderDistrict("南山区")
                    .senderAddress("科技园路1号")
                    .recipientName(address.getReceiverName())
                    .recipientPhone(address.getPhone())
                    .recipientProvince(address.getProvince())
                    .recipientCity(address.getCity())
                    .recipientDistrict(address.getDistrict())
                    .recipientAddress(address.getDetailAddress())
                    .build();

            try {
                WaybillDTO waybill = logistics.createWaybill(waybillRequest);
                if (waybill != null && waybill.getWaybillNo() != null) {
                    trackingNumber = waybill.getWaybillNo();
                }
            } catch (Exception e) {
                log.error("Failed to create waybill via logistics: {}", e.getMessage(), e);
            }
        }

        order.setExpressCompany(request.getExpressCompany());
        order.setTrackingNumber(trackingNumber);

        if ("PAID".equals(order.getStatus())) {
            order.setStatus("SHIPPED");
        }

        orderMapper.updateById(order);
        return toDTO(order);
    }

    public WaybillDTO getWaybill(Long id, Long userId, String role) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if ("MERCHANT".equals(role)) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
            if (merchant == null) {
                throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
            }
            List<Product> merchantProducts = productMapper.selectList(
                    new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchant.getId()));
            List<Long> merchantProductIds = merchantProducts.stream().map(Product::getId).collect(Collectors.toList());

            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

            boolean hasAccess = orderItems.stream().anyMatch(item -> merchantProductIds.contains(item.getProductId()));
            if (!hasAccess) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        } else if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getExpressCompany() == null || order.getTrackingNumber() == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "No logistics info");
        }

        LogisticsService logistics = logisticsFactory.getService(order.getExpressCompany());
        if (logistics == null || !logistics.isAvailable()) {
            return WaybillDTO.builder()
                    .waybillNo(order.getTrackingNumber())
                    .expressCompany(order.getExpressCompany())
                    .status("UNKNOWN")
                    .build();
        }

        return WaybillDTO.builder()
                .waybillNo(order.getTrackingNumber())
                .expressCompany(order.getExpressCompany())
                .status("CREATED")
                .build();
    }

    public List<RouteDTO> getTrackingRoutes(Long id, Long userId, String role) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if ("MERCHANT".equals(role)) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
            if (merchant == null) {
                throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
            }
            List<Product> merchantProducts = productMapper.selectList(
                    new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchant.getId()));
            List<Long> merchantProductIds = merchantProducts.stream().map(Product::getId).collect(Collectors.toList());

            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

            boolean hasAccess = orderItems.stream().anyMatch(item -> merchantProductIds.contains(item.getProductId()));
            if (!hasAccess) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        } else if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getTrackingNumber() == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "No tracking number");
        }

        LogisticsService logistics = logisticsFactory.getAvailableService(order.getExpressCompany());
        if (logistics == null) {
            return Collections.emptyList();
        }

        return logistics.getRoutes(order.getTrackingNumber());
    }

    @Transactional
    public OrderDTO cancelLogistics(Long id, Long userId, String role) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if ("MERCHANT".equals(role)) {
            Merchant merchant = merchantMapper.selectOne(
                    new LambdaQueryWrapper<Merchant>().eq(Merchant::getUserId, userId));
            if (merchant == null) {
                throw new BusinessException(ErrorCode.MERCHANT_NOT_FOUND);
            }
            List<Product> merchantProducts = productMapper.selectList(
                    new LambdaQueryWrapper<Product>().eq(Product::getMerchantId, merchant.getId()));
            List<Long> merchantProductIds = merchantProducts.stream().map(Product::getId).collect(Collectors.toList());

            List<OrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

            boolean hasAccess = orderItems.stream().anyMatch(item -> merchantProductIds.contains(item.getProductId()));
            if (!hasAccess) {
                throw new BusinessException(ErrorCode.ACCESS_DENIED);
            }
        } else if (!order.getBuyerId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getTrackingNumber() == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND, "No tracking number to cancel");
        }

        LogisticsService logistics = logisticsFactory.getAvailableService(order.getExpressCompany());
        if (logistics != null) {
            logistics.cancelWaybill(order.getTrackingNumber());
        }

        order.setTrackingNumber(null);
        order.setExpressCompany(null);
        if ("SHIPPED".equals(order.getStatus())) {
            order.setStatus("PAID");
        }

        orderMapper.updateById(order);
        return toDTO(order);
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));

        User buyer = userMapper.selectById(order.getBuyerId());
        Address address = addressMapper.selectById(order.getAddressId());

        return OrderDTO.builder()
                .id(order.getId())
                .buyerId(order.getBuyerId())
                .buyerEmail(buyer != null ? buyer.getEmail() : null)
                .address(toAddressDTO(address))
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .expressCompany(order.getExpressCompany())
                .trackingNumber(order.getTrackingNumber())
                .items(items.stream().map(this::toOrderItemDTO).collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .build();
    }

    private AddressDTO toAddressDTO(Address address) {
        if (address == null) return null;
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

    private OrderDTO.OrderItemDTO toOrderItemDTO(OrderItem item) {
        Product product = productMapper.selectById(item.getProductId());
        ProductVariant variant = item.getVariantId() != null ? variantMapper.selectById(item.getVariantId()) : null;

        return OrderDTO.OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(product != null ? product.getName() : null)
                .variantId(item.getVariantId())
                .variantSku(variant != null ? variant.getSku() : null)
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}
