package com.bootforge.inventory.repository;

import com.bootforge.inventory.entity.InventoryReservation;
import com.bootforge.inventory.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    Optional<InventoryReservation> findByOrderIdAndProductIdAndStatus(
            Long orderId,
            Long productId,
            ReservationStatus status
    );
}