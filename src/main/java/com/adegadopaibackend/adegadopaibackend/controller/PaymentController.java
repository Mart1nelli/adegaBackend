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

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<PaymentResponse> create(@PathVariable Long userId,
                                                  @Valid @RequestBody CreatePaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(userId, req));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.findByOrderId(orderId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<PaymentResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.findByUserId(userId));
    }
}
