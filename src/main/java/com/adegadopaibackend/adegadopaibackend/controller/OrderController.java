package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderResponse;
import com.adegadopaibackend.adegadopaibackend.entity.enums.OrderStatus;
import com.adegadopaibackend.adegadopaibackend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    // --- Rotas do Usuário ---

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findMyOrders() {
        return ResponseEntity.ok(orderService.findMyOrders());
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("@orderSecurity.isOwner(#orderId) or hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.findById(orderId));
    }

    // --- Rotas do Admin ---

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
}
