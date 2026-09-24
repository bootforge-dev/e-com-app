package com.bootforge.inventory.service;

import com.bootforge.inventory.dto.*;
import com.bootforge.inventory.entity.Inventory;
import com.bootforge.inventory.exception.DuplicateInventoryException;
import com.bootforge.inventory.exception.InsufficientInventoryException;
import com.bootforge.inventory.exception.ProductNotFoundException;
import com.bootforge.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new DuplicateInventoryException("Product already exists with productId: " + request.productId());
        }
        Inventory inventory = Inventory.builder()
                .productId(request.productId())
                .quantity(request.quantity())
                .reservedQuantity(0)
                .build();
        Inventory savedInventory = inventoryRepository.save(inventory);
        return toInventoryResponse(savedInventory);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {
        return toInventoryResponse(getInventoryByProduct(productId));
    }

    @Transactional(readOnly = true)
    public InventoryAvailabilityResponse checkAvailableStock(Long productId, Integer quantity) {
        Inventory inventory = getInventoryByProduct(productId);

        int availableQuantity = inventory.getQuantity() - inventory.getReservedQuantity();

        return new InventoryAvailabilityResponse(
                productId,
                quantity,
                availableQuantity,
                availableQuantity >= quantity
        );
    }

    @Transactional
    public InventoryResponse updateInventory(Long productId, UpdateInventoryRequest request) {
        Inventory inventory = getInventoryByProduct(productId);
        inventory.setQuantity(request.quantity());
        return toInventoryResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResponse reserveStock(Long productId, Integer quantity) {
        //get inventory by productId
        Inventory inventory = getInventoryByProduct(productId);

        //check available quantity
        int availableQuantity = inventory.getQuantity() - inventory.getReservedQuantity();

        //check requested quantity is available or not
        if (availableQuantity < quantity) {
            throw new InsufficientInventoryException(
                    "Insufficient stock for productId: " + productId
                            + ". Available: " + availableQuantity
                            + ", Requested: " + quantity
            );
        }
        //update reserve quantity
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        return toInventoryResponse(inventory);
    }

    @Transactional
    public InventoryResponse releaseStock(Long productId, Integer quantity) {
        //get inventory by productId
        Inventory inventory = getInventoryByProduct(productId);

        if (inventory.getReservedQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Cannot release more stock than reserved for productId: "
                            + productId
            );
        }
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        return toInventoryResponse(inventory);
    }

    @Transactional
    public InventoryResponse confirmStock(Long productId, Integer quantity) {
        //get inventory by productId
        Inventory inventory = getInventoryByProduct(productId);

        if (inventory.getReservedQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Cannot confirm more stock than reserved for productId: "
                            + productId
            );
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);

        inventory.setQuantity(inventory.getQuantity() - quantity);
        return toInventoryResponse(inventory);
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> getAllInventories(Pageable pageable) {
        Page<Inventory> pageInventory = inventoryRepository.findAll(pageable);
        List<InventoryResponse> inventories = pageInventory.getContent().stream()
                .map(this::toInventoryResponse).toList();
        return new PageResponse<>(
                inventories,
                pageInventory.getNumber(),
                pageInventory.getSize(),
                pageInventory.getTotalElements(),
                pageInventory.getTotalPages()
        );
    }

    private Inventory getInventoryByProduct(Long productId) {
        return inventoryRepository.findByProductId(productId).orElseThrow(
                () -> new ProductNotFoundException("Product not found with the productId: " + productId)
        );
    }

    private InventoryResponse toInventoryResponse(Inventory inventory) {
        Integer availableQuantity =
                inventory.getQuantity() - inventory.getReservedQuantity();
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(availableQuantity)
                .build();
    }
}
