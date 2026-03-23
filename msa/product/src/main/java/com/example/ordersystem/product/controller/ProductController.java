package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductUpdateStockDto;
import com.example.ordersystem.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> productCreate(@ModelAttribute ProductRegisterDto dto,
                                                             @RequestPart("image") MultipartFile image,
                                                             @RequestHeader("X-User-Id") String userId){
        Product product = productService.productCreate(dto, image, userId);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "success");
        body.put("productId", product.getId());
        body.put("imageUrl", product.getImageUrl());
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ProductResDto>> productList(){
        return new ResponseEntity<>(productService.productList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResDto> productDetail(@PathVariable Long id) {
        return new ResponseEntity<>(productService.productDetail(id), HttpStatus.OK);
    }

    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDto productUpdateStockDto,
                                         @RequestHeader("X-User-Id") String userId){
        Product product = productService.decreaseStockQuantity(productUpdateStockDto.getProductId(), productUpdateStockDto.getProductQuantity());
        return new ResponseEntity<>(product.getId(), HttpStatus.OK);
    }
}
