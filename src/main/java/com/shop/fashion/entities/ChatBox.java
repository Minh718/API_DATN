package com.shop.fashion.entities;

import java.time.LocalDateTime;

import com.shop.fashion.utils.DateTimeUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ChatBox {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String userId;
    // private String adminId;
    private LocalDateTime createdAt = DateTimeUtil.getCurrentVietnamTime();
    private LocalDateTime updatedAt = DateTimeUtil.getCurrentVietnamTime();
    private boolean userReaded = true;
    private boolean adminReaded = true;
}
