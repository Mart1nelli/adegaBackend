package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderReviewRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderReviewResponse;
import com.adegadopaibackend.adegadopaibackend.service.OrderReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order-reviews")
public class OrderReviewController {

    private final OrderReviewService orderReviewService;

    // --- Rotas do Usuário ---

    @PostMapping("/order/{orderId}")
    public ResponseEntity<OrderReviewResponse> create(@PathVariable Long orderId,
                                                      @Valid @RequestBody CreateOrderReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderReviewService.create(orderId, req));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderReviewResponse>> findMyOrderReviews() {
        return ResponseEntity.ok(orderReviewService.findMyOrderReviews());
    }

    // --- Público ---

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderReviewResponse>> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderReviewService.findByOrderId(orderId));
    }

    // --- Admin ---

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderReviewResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderReviewService.findByUserId(userId));
    }
}
