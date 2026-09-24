package com.bootforge.order.service;

import com.bootforge.order.client.InventoryClient;
import com.bootforge.order.client.ProductClient;
import com.bootforge.order.dto.*;
import com.bootforge.order.entity.Order;
import com.bootforge.order.entity.OrderItem;
import com.bootforge.order.exception.OrderNotFoundException;
import com.bootforge.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.customerId())
                .status(OrderStatus.PENDING)
                .currency("INR")
                .totalAmount(BigDecimal.ZERO)
                .build();

        ShippingAddressRequest address =
                request.shippingAddress();

        order.setAddressLine1(address.addressLine1());
        order.setCity(address.city());
        order.setState(address.state());
        order.setPostalCode(address.postalCode());
        order.setCountry(address.country());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            //get product
            ProductResponse product = productClient.getProductById(itemRequest.productId());

            //validate the product
            validateProduct(product);

            BigDecimal itemTotal = product.price().multiply(BigDecimal.valueOf(itemRequest.quantity()));

            //create order item
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.id())
                    .productName(product.name())
                    .sku(product.sku())
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.price())
                    .totalPrice(itemTotal)
                    .build();

            order.addItem(orderItem);
            totalAmount = totalAmount.add(itemTotal);
        }
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        for (OrderItem item : savedOrder.getItems()) {
            inventoryClient.reserveStock(
                    item.getProductId(),
                    item.getQuantity(),
                    savedOrder.getId()
            );
        }
        savedOrder.setStatus(OrderStatus.INVENTORY_RESERVED);

        return toOrderResponse(savedOrder);
    }

    private void validateProduct(ProductResponse product) {
        if (product == null) {
            throw new IllegalStateException("Product service returned empty response");
        }
        if (!"ACTIVE".equalsIgnoreCase(product.status())) {
            throw new IllegalStateException("Product is not available: " + product.id());
        }
    }


    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + orderId)
                );
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrders(Pageable pageable) {
        Page<Order> orderPage = orderRepository.findAll(pageable);
        List<OrderResponse> data = orderPage.getContent().stream().map(this::toOrderResponse).toList();

        return new PageResponse<>(
                data,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrderByCustomer(Long customerId, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        List<OrderResponse> data = orderPage.getContent().stream().map(this::toOrderResponse).toList();

        return new PageResponse<>(
                data,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages()
        );
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new OrderNotFoundException("Order not found with id: " + orderId));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return toOrderResponse(order);
        }

        if (order.getStatus() != OrderStatus.INVENTORY_RESERVED) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            inventoryClient.releaseStock(
                    item.getProductId(),
                    item.getQuantity(),
                    order.getId()
            );
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toOrderResponse(order);
    }


    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items =
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getProductId(),
                                item.getProductName(),
                                item.getSku(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getTotalPrice()
                        )).toList();
        ShippingAddressResponse address = new ShippingAddressResponse(
                order.getAddressLine1(),
                order.getCity(),
                order.getState(),
                order.getPostalCode(),
                order.getCountry()
        );

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                items,
                address,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private record ReserveItem(
            Long productId,
            Integer quantity
    ) {
    }

}
