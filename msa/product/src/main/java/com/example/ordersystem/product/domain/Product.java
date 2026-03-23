package com.example.ordersystem.product.domain;

import com.example.ordersystem.common.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "product")
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private String category;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    private Integer price;
    private Integer stockQuantity;

    @Column(nullable = false)
    private Long memberId;

    public void decreaseStockQuantity(int quantity){
        if (this.stockQuantity == null) {
            this.stockQuantity = 0;
        }
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stockQuantity = this.stockQuantity - quantity;
    }
}
