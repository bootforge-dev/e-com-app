package com.bootforge.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Sku must be required")
        String sku,

        @NotBlank(message = "Product name must be required")
        @Length(min = 3,message = "Name has minimum 3 character length")
        String name,

        @NotBlank(message = "Description must be required")
        @Length(max = 200,message = "Description length must be below 200 characters")
        String description,

        @NotNull(message = "Price must be required")
        @Positive(message = "Price not be negative")
        BigDecimal price,

        @NotBlank(message = "Category must be required")
        String category,

        String brand
) {
}
