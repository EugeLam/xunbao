package com.shop.shop.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common errors (1xxx)
    BAD_REQUEST(400, "err.bad_request"),
    UNAUTHORIZED(401, "err.unauthorized"),
    FORBIDDEN(403, "err.forbidden"),
    NOT_FOUND(404, "err.not_found"),
    INTERNAL_ERROR(500, "err.internal"),

    // Business errors (10xx)
    EMAIL_ALREADY_EXISTS(1001, "err.email_exists"),
    INVALID_CREDENTIALS(1002, "err.invalid_credentials"),
    INVALID_REFRESH_TOKEN(1003, "err.invalid_refresh_token"),
    TOKEN_EXPIRED(1004, "err.token_expired"),
    USER_NOT_FOUND(1010, "err.user_not_found"),
    PRODUCT_NOT_FOUND(1011, "err.product_not_found"),
    MERCHANT_NOT_FOUND(1012, "err.merchant_not_found"),
    ACCESS_DENIED(1013, "err.access_denied"),
    INSUFFICIENT_STOCK(1014, "err.insufficient_stock"),
    CART_ITEM_NOT_FOUND(1015, "err.cart_item_not_found"),
    ADDRESS_NOT_FOUND(1016, "err.address_not_found"),
    ORDER_NOT_FOUND(1017, "err.order_not_found"),
    VARIANT_NOT_FOUND(1018, "err.variant_not_found"),
    ORDER_ITEM_NOT_FOUND(1019, "err.order_item_not_found"),
    SKU_ALREADY_EXISTS(1020, "err.sku_exists"),
    ALREADY_FAVORITED(1021, "err.already_favorited"),
    REVIEW_NOT_FOUND(1022, "err.review_not_found"),
    CATEGORY_NOT_FOUND(1023, "err.category_not_found"),
    INVALID_STATUS(1024, "err.invalid_status");

    private final int httpStatus;
    private final String messageKey;
}
