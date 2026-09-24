package com.bootforge.productservice.controller;

import com.bootforge.productservice.dto.CreateProductRequest;
import com.bootforge.productservice.dto.PageResponse;
import com.bootforge.productservice.dto.ProductResponse;
import com.bootforge.productservice.entity.ProductStatus;
import com.bootforge.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
@Tag(name = "Product", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Fetch Product by Id")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    @Operation(summary = "Fetch All products by pagination")
    public ResponseEntity<PageResponse<ProductResponse>> getAllProducts(
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product by Id")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody CreateProductRequest request,
            @RequestParam ProductStatus status
            ) {
        return ResponseEntity.ok(
                productService.updateProduct(id, request, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product by Id")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search product by Name")
    public ResponseEntity<PageResponse<ProductResponse>> searchByProductName(
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(productService.searchByProductName(name, pageable));
    }

    @GetMapping("/status")
    @Operation(summary = "Get products by status")
    public ResponseEntity<Page<ProductResponse>> getProductsByStatus(
            @RequestParam ProductStatus status,
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(productService.getProductsByStatus(status, pageable));
    }



}
