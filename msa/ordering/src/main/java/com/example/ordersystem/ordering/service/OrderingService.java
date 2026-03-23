package com.example.ordersystem.ordering.service;

import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.ProductDto;
import com.example.ordersystem.ordering.dto.ProductUpdateStockDto;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderingService {
    private final OrderingRepository orderingRepository;
    private final ProductFeign productFeign;

    public OrderingService(OrderingRepository orderingRepository, ProductFeign productFeign) {
        this.orderingRepository = orderingRepository;
        this.productFeign = productFeign;
    }

    public Ordering orderCreate(OrderCreateDto orderDto, String userId){
        ProductDto productDto = productFeign.getProductById(orderDto.getProductId());
        int quantity = orderDto.getProductCount();

        if(productDto.getStockQuantity() < quantity){
            throw new IllegalArgumentException("재고 부족");
        }

        ProductUpdateStockDto dto = ProductUpdateStockDto.builder()
                .productId(orderDto.getProductId())
                .productQuantity(orderDto.getProductCount())
                .build();
        productFeign.updateProductStock(dto, userId);

        Ordering ordering = Ordering.builder()
                .memberId(Long.parseLong(userId))
                .productId(orderDto.getProductId())
                .quantity(orderDto.getProductCount())
                .build();
        orderingRepository.save(ordering);
        return ordering;
    }
}
