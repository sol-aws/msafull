package com.example.ordersystem.product.controller;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductUpdateStockDto;
import com.example.ordersystem.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/create", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> productCreate(@ModelAttribute ProductRegisterDto dto,
                                                             @RequestPart("image") MultipartFile image,
                                                             @RequestHeader("X-User-Id") String userId){
        log.info("POST /product/create called. userId={}", userId);
        Product product = productService.productCreate(dto, image, userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "상품등록 성공");
        result.put("productId", product.getId());
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ProductResDto>> productList(){
        return new ResponseEntity<>(productService.productList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResDto> productDetail(@PathVariable Long id) {
        return new ResponseEntity<>(productService.productDetail(id), HttpStatus.OK);
    }

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource resource = productService.loadImageAsResource(fileName);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            String contentType = Files.probeContentType(Path.of(resource.getFile().getAbsolutePath()));
            if (contentType != null) {
                mediaType = MediaType.parseMediaType(contentType);
            }
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);
    }

    @PutMapping("/updatestock")
    public ResponseEntity<?> updateStock(@RequestBody ProductUpdateStockDto productUpdateStockDto,
                                         @RequestHeader("X-User-Id") String userId){
        Product product = productService.decreaseStockQuantity(productUpdateStockDto.getProductId(), productUpdateStockDto.getProductQuantity());
        return new ResponseEntity<>(product.getId(), HttpStatus.OK);
    }
}
