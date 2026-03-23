package com.example.ordersystem.product.service;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductUpdateStockDto;
import com.example.ordersystem.product.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class ProductService {
    private static final String DEFAULT_PRODUCT_IMAGE_URL = "https://via.placeholder.com/600x600?text=No+Image";

    private final ProductRepository productRepository;
    private final S3Service s3Service;

    public ProductService(ProductRepository productRepository, S3Service s3Service) {
        this.productRepository = productRepository;
        this.s3Service = s3Service;
    }

    public Product productCreate(ProductRegisterDto dto, String userId) throws IOException {
        Long memberId;
        try {
            memberId = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("로그인 사용자 정보가 올바르지 않습니다. 다시 로그인해주세요.");
        }

        String finalImageUrl = resolveImageUrl(dto);
        return productRepository.save(dto.toEntity(memberId, finalImageUrl));
    }

    @Transactional(readOnly = true)
    public ProductResDto productDetail(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        return toDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResDto> productList(){
        return productRepository.findAllByOrderByIdDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public Product updateStockQuantity(ProductUpdateStockDto productUpdateStockDto){
        Product product = productRepository.findById(productUpdateStockDto.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        if (productUpdateStockDto.getProductQuantity() == null) {
            throw new IllegalArgumentException("구매 수량이 없습니다.");
        }
        product.updateStockQuantity(productUpdateStockDto.getProductQuantity());
        return product;
    }

    public Product purchaseProduct(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        product.updateStockQuantity(1);
        return product;
    }

    @KafkaListener(topics = "update-stock-topic", containerFactory = "kafkaListener")
    public void stockConsumer(String message){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ProductUpdateStockDto dto = objectMapper.readValue(message, ProductUpdateStockDto.class);
            this.updateStockQuantity(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String resolveImageUrl(ProductRegisterDto dto) throws IOException {
        String finalImageUrl = dto.getImageUrl();

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            try {
                return s3Service.uploadFile(dto.getImageFile());
            } catch (Exception e) {
                if (finalImageUrl != null && !finalImageUrl.isBlank()) {
                    return finalImageUrl;
                }
                return DEFAULT_PRODUCT_IMAGE_URL;
            }
        }

        if (finalImageUrl == null || finalImageUrl.isBlank()) {
            return DEFAULT_PRODUCT_IMAGE_URL;
        }
        return finalImageUrl;
    }

    private ProductResDto toDto(Product product){
        return ProductResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}
