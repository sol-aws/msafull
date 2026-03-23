package com.example.ordersystem.ordering.controller;

import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.ordering.dto.OrderCreateDto;
import com.example.ordersystem.ordering.service.OrderingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ordering")
public class OrderingController {

    private final OrderingService orderingService;

    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody OrderCreateDto dto,
                                    @RequestHeader("X-User-Id") String userId) {

        Ordering order = orderingService.orderCreate(dto, userId);
        return ResponseEntity.ok(order.getId());
    }
}
