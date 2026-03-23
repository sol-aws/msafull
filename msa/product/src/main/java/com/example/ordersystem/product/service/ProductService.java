package com.example.ordersystem.product.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public ProductService(ProductRepository productRepository, AmazonS3 amazonS3) {
        this.productRepository = productRepository;
        this.amazonS3 = amazonS3;
    }

    public Product productCreate(ProductRegisterDto dto, MultipartFile image, String userId){
        String imageUrl = uploadImage(image);
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .imageUrl(imageUrl)
                .memberId(Long.parseLong(userId))
                .build();
        return productRepository.save(product);
    }

    public List<ProductResDto> productList(){
        return productRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public ProductResDto productDetail(Long id){
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        return toDto(product);
    }

    public Product decreaseStockQuantity(Long productId, int quantity){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("없는 상품입니다."));
        product.decreaseStockQuantity(quantity);
        return product;
    }

    private ProductResDto toDto(Product product) {
        return ProductResDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }

    private String uploadImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("상품 이미지는 필수입니다.");
        }
        try {
            String originalName = image.getOriginalFilename() == null ? "product-image" : image.getOriginalFilename();
            String safeName = originalName.replaceAll("\s+", "-");
            String key = "products/" + UUID.randomUUID() + "-" + safeName;
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(image.getSize());
            metadata.setContentType(image.getContentType());
            amazonS3.putObject(bucket, key, image.getInputStream(), metadata);
            return amazonS3.getUrl(bucket, key).toString();
        } catch (IOException e) {
            throw new IllegalStateException("이미지 파일 읽기에 실패했습니다.", e);
        } catch (Exception e) {
            throw new IllegalStateException("S3 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
