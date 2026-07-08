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
import java.util.Map;

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

    // --- Rota Pública do Webhook do Mercado Pago ---

    @PostMapping("/webhook")
    public ResponseEntity<Void> receberWebhook(
            @RequestParam(value = "data.id", required = false) Long dataId,
            @RequestParam(value = "type", required = false) String type,
            @RequestBody(required = false) Map<String, Object> payload) {

        // Encaminha a notificação direto para o service processar a aprovação
        paymentService.processarNotificacaoWebhook(dataId, type, payload);

        // Retorna 200 OK exigido pelo Mercado Pago
        return ResponseEntity.ok().build();
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
