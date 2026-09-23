package com.bootforge.productservice.service;

import com.bootforge.productservice.dto.CreateProductRequest;
import com.bootforge.productservice.dto.ProductResponse;
import com.bootforge.productservice.entity.Product;
import com.bootforge.productservice.entity.ProductStatus;
import com.bootforge.productservice.exception.DuplicateProductException;
import com.bootforge.productservice.exception.ProductNotFoundException;
import com.bootforge.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if(productRepository.existsBySku(request.sku())){
            throw new DuplicateProductException("Product already existed with sku: "+request.sku());
        }
        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(request.category())
                .brand(request.brand())
                .status(ProductStatus.ACTIVE)
                .build();

        Product savedProduct = productRepository.save(product);
        return toProductResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return toProductResponse(getProduct(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::toProductResponse);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, CreateProductRequest request) {
        Product product = getProduct(id);

        if(productRepository.existsBySkuAndIdNot(request.sku(), id)){
            throw new DuplicateProductException(
                    "Product already exists with sku: " + request.sku()
            );
        }

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        product.setBrand(request.brand());

        Product updatedProduct = productRepository.save(product);
        return toProductResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProduct(id);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchByProductName(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndStatus(name,ProductStatus.ACTIVE,pageable)
                .map(this::toProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByStatus(ProductStatus status, Pageable pageable) {
        return productRepository.findByStatus(status,pageable);
    }

    private Product getProduct(Long id){
        return productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product not found with the productId: "+ id)
        );
    }

    private ProductResponse toProductResponse(Product product){
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .brand(product.getBrand())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .build();
    }


}
