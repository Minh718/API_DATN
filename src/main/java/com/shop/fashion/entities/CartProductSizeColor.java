package com.shop.fashion.entities;

import java.time.LocalDateTime;

import com.shop.fashion.utils.DateTimeUtil;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartProductSizeColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer quantity;
    @Builder.Default
    private LocalDateTime createdAt = DateTimeUtil.getCurrentVietnamTime();
    @Builder.Default
    private LocalDateTime updateAt = DateTimeUtil.getCurrentVietnamTime();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    private Cart cart;

    private Long productSizeColorId;
}