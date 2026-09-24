package com.bootforge.inventory.controller;

import com.bootforge.inventory.dto.*;
import com.bootforge.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventories")
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create inventory")
    public ResponseEntity<InventoryResponse> createInventory(
            @Valid @RequestBody CreateInventoryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventory(request));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory by productId")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(
            @PathVariable
            Long productId){
        return ResponseEntity.ok(inventoryService.getInventoryByProductId(productId));
    }

    @GetMapping("/{productId}/availability")
    @Operation(summary = "Check is stock available or not")
    public ResponseEntity<InventoryAvailabilityResponse> checkAvailableStock(
            @PathVariable
            Long productId,

            @RequestParam
            @Positive(message = "Quantity must be positive")
            Integer quantity) {
        return ResponseEntity.ok(inventoryService.checkAvailableStock(productId, quantity));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update inventory")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateInventoryRequest request){
           return ResponseEntity.ok(inventoryService.updateInventory(productId, request));
    }

    @Operation(summary = "Reserve stock")
    @PostMapping("/{productId}/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(
            @PathVariable
            Long productId,

            @RequestParam
            @Positive(message = "Quantity must be positive")
            Integer quantity
    ){
        return ResponseEntity.ok(inventoryService.reserveStock(productId, quantity));
    }

    @Operation(summary = "Release stock")
    @PostMapping("/{productId}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable
            Long productId,

            @RequestParam
            @Positive(message = "Quantity must be positive")
            Integer quantity
    ){
        return ResponseEntity.ok(inventoryService.releaseStock(productId, quantity));
    }

    @Operation(summary = "Confirm stock")
    @PostMapping("/{productId}/confirm")
    public ResponseEntity<InventoryResponse> confirmStock(
            @PathVariable
            Long productId,

            @RequestParam
            @Positive(message = "Quantity must be positive")
            Integer quantity
    ){
        return ResponseEntity.ok(inventoryService.confirmStock(productId, quantity));
    }

    @GetMapping
    @Operation(summary = "Get all inventories")
    public ResponseEntity<PageResponse<InventoryResponse>> getAllInventories(
            @ParameterObject
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)Pageable pageable){
        return ResponseEntity.ok(inventoryService.getAllInventories(pageable));
    }

}
