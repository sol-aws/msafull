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
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(length = 1000)
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Long memberId;

    public void updateStockQuantity(int stockQuantity){
        this.stockQuantity = this.stockQuantity - stockQuantity;
    }
}
