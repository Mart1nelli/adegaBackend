package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentMethodRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdatePaymentMethodRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentMethodResponse;
import com.adegadopaibackend.adegadopaibackend.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentMethodResponse> create(@Valid @RequestBody CreatePaymentMethodRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentMethodService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> findAll() {
        return ResponseEntity.ok(paymentMethodService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentMethodService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentMethodResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody UpdatePaymentMethodRequest req) {
        return ResponseEntity.ok(paymentMethodService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
