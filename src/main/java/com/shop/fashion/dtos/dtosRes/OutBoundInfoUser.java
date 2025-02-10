package com.shop.fashion.dtos.dtosRes;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * OutBoundInfoUser
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OutBoundInfoUser(String id, String email, boolean verifiedEmail,
        String familyName, String name, String givenName, String picture, String hd) {
}