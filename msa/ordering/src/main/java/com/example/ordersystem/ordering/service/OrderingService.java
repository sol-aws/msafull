package com.example.ordersystem.ordering.service;

import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.dto.ProductDto;
import com.example.ordersystem.ordering.dto.ProductUpdateStockDto;
import com.example.ordersystem.ordering.repository.OrderingRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final RestTemplate restTemplate;

    public OrderingService(OrderingRepository orderingRepository, RestTemplate restTemplate) {
        this.orderingRepository = orderingRepository;
        this.restTemplate = restTemplate;
    }

    public Ordering orderCreate(OrderCreateDto orderDto, String userId) {

        // 1. 상품 조회
        String getUrl = "http://product-service.soldesk.svc.cluster.local/product/" + orderDto.getProductId();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId);

        ResponseEntity<ProductDto> response = restTemplate.exchange(
                getUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ProductDto.class
        );

        ProductDto product = response.getBody();

        if (product == null) {
            throw new RuntimeException("상품 없음");
        }

        if (product.getStockQuantity() < orderDto.getProductCount()) {
            throw new RuntimeException("재고 부족");
        }

        // 2. 재고 차감
        String updateUrl = "http://product-service.soldesk.svc.cluster.local/product/updatestock";

        headers.setContentType(MediaType.APPLICATION_JSON);

        ProductUpdateStockDto updateDto = ProductUpdateStockDto.builder()
                .productId(orderDto.getProductId())
                .productQuantity(orderDto.getProductCount())
                .build();

        restTemplate.exchange(
                updateUrl,
                HttpMethod.PUT,
                new HttpEntity<>(updateDto, headers),
                Void.class
        );

        // 3. 주문 저장
        Ordering order = Ordering.builder()
                .memberId(Long.parseLong(userId))
                .productId(orderDto.getProductId())
                .quantity(orderDto.getProductCount())
                .build();

        return orderingRepository.save(order);
    }
}
