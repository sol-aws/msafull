package com.example.ordersystem.product.service;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.dto.ProductUpdateStockDto;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final S3Service s3Service;

    public ProductService(ProductRepository productRepository, S3Service s3Service) {
        this.productRepository = productRepository;
        this.s3Service = s3Service;
    }

    public Product productCreate(ProductRegisterDto dto, String userId) {
        String finalImageUrl = dto.getImageUrl();

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            try {
                finalImageUrl = s3Service.uploadFile(dto.getImageFile());
            } catch (Exception e) {
                System.out.println("S3 업로드 실패, DB 저장은 계속 진행합니다: " + e.getMessage());
                e.printStackTrace();
                finalImageUrl = dto.getImageUrl();
            }
        }

        Long memberId = 1L;
        try {
            if (userId != null && !userId.isBlank()) {
                memberId = Long.parseLong(userId);
            }
        } catch (NumberFormatException e) {
            System.out.println("X-User-Id 파싱 실패, 기본 사용자 1L로 저장합니다: " + userId);
        }

        Product savedProduct = productRepository.save(dto.toEntity(memberId, finalImageUrl));
        System.out.println("상품 등록 완료 - productId=" + savedProduct.getId() + ", memberId=" + memberId);
        return savedProduct;
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
