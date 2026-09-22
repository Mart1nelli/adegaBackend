package com.adegadopaibackend.adegadopaibackend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.CreateTransparentPaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;
import com.adegadopaibackend.adegadopaibackend.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    @PostMapping("/transparent")
    public ResponseEntity<PaymentResponse> createTransparent(
            @Valid @RequestBody CreateTransparentPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createTransparent(req));
    }

    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> findMyPayments() {
        return ResponseEntity.ok(paymentService.findMyPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.findByOrderId(orderId));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<PaymentResponse> syncPaymentStatus(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.syncPaymentStatus(id));
    }

    // --- Rota Pública do Webhook do Mercado Pago ---

    @PostMapping("/webhook")
    public ResponseEntity<Void> receberWebhook(
            @RequestParam(required = false) Map<String, String> queryParams,
            @RequestBody(required = false) Map<String, Object> payload) {

        paymentService.processarNotificacaoWebhook(queryParams, payload);
        return ResponseEntity.ok().build();
    }

    // --- Rotas do Admin ---

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.findByUserId(userId));
    }
}
