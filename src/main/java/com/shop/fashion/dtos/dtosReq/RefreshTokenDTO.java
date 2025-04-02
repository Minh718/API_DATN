package com.shop.fashion.dtos.dtosReq;

import jakarta.validation.constraints.NotNull;

public record RefreshTokenDTO(@NotNull String refreshToken) {
}