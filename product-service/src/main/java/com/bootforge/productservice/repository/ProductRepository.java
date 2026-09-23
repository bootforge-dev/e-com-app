package com.bootforge.productservice.repository;

import com.bootforge.productservice.dto.ProductResponse;
import com.bootforge.productservice.entity.Product;
import com.bootforge.productservice.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);
    boolean existsBySkuAndIdNot(String sku, Long id);

    Page<Product> findByNameContainingIgnoreCaseAndStatus(String name, ProductStatus status, Pageable pageable);

    Page<ProductResponse> findByStatus(ProductStatus status,Pageable pageable);
}
