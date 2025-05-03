package com.shop.fashion.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * CartProduct
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long quantity;
    @Builder.Default
    private long price = 25L;
    private String name;
    private String image;
    private String color;
    @Column(name = "rating")

    private Integer rating;
    @Builder.Default
    @Column(name = "is_rating")
    private Boolean isRating = false;

    private String size;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_size_color_id", referencedColumnName = "id")
    private ProductSizeColor productSizeColor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

}