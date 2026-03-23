package com.example.ordersystem.product.service;

import com.example.ordersystem.product.domain.Product;
import com.example.ordersystem.product.dto.ProductRegisterDto;
import com.example.ordersystem.product.dto.ProductResDto;
import com.example.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;

    @Value("${app.upload-dir:/app/uploads}")
    private String uploadDir;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product productCreate(ProductRegisterDto dto, MultipartFile image, String userId){
        log.info("productCreate start. name={}, category={}, userId={}, imageSize={}",
                dto.getName(), dto.getCategory(), userId, image == null ? 0 : image.getSize());

        String imageUrl = saveImage(image);

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .imageUrl(imageUrl)
                .memberId(Long.parseLong(userId))
                .build();

        Product saved = productRepository.save(product);
        log.info("productCreate success. productId={}", saved.getId());
        return saved;
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

    public Resource loadImageAsResource(String fileName) {
        try {
            Path uploadPath = getUploadPath();
            Path filePath = uploadPath.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new EntityNotFoundException("이미지를 찾을 수 없습니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("이미지 경로가 올바르지 않습니다.", e);
        }
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

    private String saveImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("상품 이미지는 필수입니다.");
        }
        try {
            Path uploadPath = getUploadPath();
            Files.createDirectories(uploadPath);

            String originalName = StringUtils.cleanPath(image.getOriginalFilename() == null ? "product-image" : image.getOriginalFilename());
            String extension = "";
            int index = originalName.lastIndexOf('.');
            if (index >= 0) {
                extension = originalName.substring(index);
            }
            String fileName = UUID.randomUUID() + extension;
            Path targetPath = uploadPath.resolve(fileName);

            Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("local image save success. path={}", targetPath);
            return "/product/images/" + fileName;
        } catch (IOException e) {
            log.error("로컬 이미지 저장 실패", e);
            throw new IllegalArgumentException("이미지 저장에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private Path getUploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}
