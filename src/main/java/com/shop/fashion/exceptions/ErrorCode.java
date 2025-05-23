package com.shop.fashion.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    INVALIDTE_FIELD(1022, "Field wrong syntax", HttpStatus.BAD_REQUEST),
    DONT_HAVE_PERMISSION(1035, "Don't have permission", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    TOKEN_EXPIRED(1009, "Token expired", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1009, "Error Token", HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST(1042, "Invalid request", HttpStatus.BAD_REQUEST),
    SUBCATEGORY_NOT_EXISTED(1021, "Subcategory not existed", HttpStatus.NOT_FOUND),
    BRAND_NOT_EXISTED(1041, "Brand not existed", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_EXISTED(1042, "Product not existed", HttpStatus.NOT_FOUND),
    PHONE_NOT_EXISTED(1043, "Phone number not existed", HttpStatus.NOT_FOUND),
    INVALID_OTP(1044, " Otp is invalid", HttpStatus.NOT_FOUND),
    OTP_IS_EXPIRED(1044, " Otp is expired", HttpStatus.NOT_FOUND),
    PHONE_REGISTERED(1045, "This phone is registered with system", HttpStatus.NOT_FOUND),
    OTP_IS_SENDING(1046, "Otp is sending to your phone", HttpStatus.NOT_FOUND),
    PRODUCT_SIZE_NOT_EXISTED(1048, "Product size not existed", HttpStatus.NOT_FOUND),
    CART_PRODUCT_SIZE_NOT_EXISTED(1049, "Cart product size not existed", HttpStatus.NOT_FOUND),
    PRODUCT_SIZE_COLOR_NOT_EXISTED(1050, "Product size color not existed", HttpStatus.NOT_FOUND),
    PLEASE_RELOAD_PAGE(1051, "please reload page", HttpStatus.BAD_REQUEST),
    VOUCHER_DONT_BELONG_TO_USER(1052, "Voucher don't belong to user", HttpStatus.BAD_REQUEST),
    VOUCHER_IS_USED_ALREADY(1053, "Voucher is used already", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(1054, "BAD_REQUEST", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_ACTIVE(1055, "Voucher is not active", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_FOUND(1056, "Voucher not found", HttpStatus.NOT_FOUND),
    SLIDE_NOT_FOUND(1056, "Slide not found", HttpStatus.NOT_FOUND),
    RECHECKOUT_FAILED(1057, "Recheckout failed", HttpStatus.BAD_REQUEST),
    ERROR_SYSTEM(1058, "System error", HttpStatus.INTERNAL_SERVER_ERROR),
    ERROR_PAYMENT(1059, "Error payment", HttpStatus.BAD_REQUEST),
    CHAT_BOX_NOT_FOUND(1060, "Chat box not found", HttpStatus.NOT_FOUND),
    INVALID_REFRESHTOKEN(1061, "Error Token, Please relogin", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_EXISTED(1062, "Category not existed", HttpStatus.NOT_FOUND),
    COLOR_NOT_EXISTED(1063, "Color not existed", HttpStatus.NOT_FOUND),

    QUANTITY_NOT_ENOUGH(1047, "Quantity not enough", HttpStatus.BAD_REQUEST);

    ErrorCode(

            int code, String message,
            HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}