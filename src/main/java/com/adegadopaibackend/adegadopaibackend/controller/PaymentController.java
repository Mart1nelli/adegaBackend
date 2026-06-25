package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;
import com.adegadopaibackend.adegadopaibackend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    // --- Rotas do Usuário ---

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(req));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> findMyPayments() {
        return ResponseEntity.ok(paymentService.findMyPayments());
    }

    // --- Rotas do Admin ---

    @GetMapping("/admin/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.findByOrderId(orderId));
    }

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.findByUserId(userId));
    }
}
