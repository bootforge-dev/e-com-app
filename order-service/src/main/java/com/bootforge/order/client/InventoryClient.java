package com.bootforge.order.client;

import com.bootforge.order.dto.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping("/api/v1/inventories/{productId}/reserve")
    InventoryResponse reserveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam Long orderId
    );

    @PostMapping("/api/v1/inventories/{productId}/release")
    public ResponseEntity<InventoryResponse> releaseStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam Long orderId
    );

}
