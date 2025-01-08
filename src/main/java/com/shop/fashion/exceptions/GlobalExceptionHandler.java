package com.shop.fashion.exceptions;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.shop.fashion.dtos.dtosRes.ApiErrorRes;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiErrorRes> handlingRuntimeException(RuntimeException exception) {
        ApiErrorRes apiErrorRes = new ApiErrorRes();
        System.out.println(exception);
        apiErrorRes.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiErrorRes.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

        return ResponseEntity.badRequest().body(apiErrorRes);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiErrorRes> handlingAccessDeniedException(AccessDeniedException exception) {
        ApiErrorRes apiErrorRes = new ApiErrorRes();
        apiErrorRes.setCode(ErrorCode.DONT_HAVE_PERMISSION.getCode());
        apiErrorRes.setMessage(ErrorCode.DONT_HAVE_PERMISSION.getMessage());

        return ResponseEntity.badRequest().body(apiErrorRes);
    }

    @ExceptionHandler(value = CustomException.class)
    ResponseEntity<ApiErrorRes> handlingCustomException(CustomException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiErrorRes apiErrorRes = new ApiErrorRes();

        apiErrorRes.setCode(errorCode.getCode());
        if (errorCode.getMessage().equals("BAD_REQUEST"))
            apiErrorRes.setMessage(exception.getMessage());
        else
            apiErrorRes.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiErrorRes);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorRes> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.INVALIDTE_FIELD;
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        ApiErrorRes apiErrorRes = new ApiErrorRes();
        apiErrorRes.setCode(errorCode.getCode());
        apiErrorRes.setMessage(fieldErrors.get(0).getDefaultMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiErrorRes);
    }
}