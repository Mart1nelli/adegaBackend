package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    // Rotas do Usuário (userId extraído do token)
    PaymentResponse create(CreatePaymentRequest req);
    List<PaymentResponse> findMyPayments();

    // Rotas do Admin (userId explícito)
    List<PaymentResponse> findByOrderId(Long orderId);
    List<PaymentResponse> findByUserId(Long userId);
}
