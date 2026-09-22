package com.adegadopaibackend.adegadopaibackend.service;

import java.util.List;
import java.util.Map;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.CreateTransparentPaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;

public interface PaymentService {

    // Rotas do Usuário (userId extraído do token)
    PaymentResponse create(CreatePaymentRequest req);
    PaymentResponse createTransparent(CreateTransparentPaymentRequest req);
    PaymentResponse findById(Long id);
    List<PaymentResponse> findMyPayments();
    List<PaymentResponse> findByOrderId(Long orderId);
    PaymentResponse syncPaymentStatus(Long id);

    // Rota do Webhook (Mercado Pago)
    void processarNotificacaoWebhook(Map<String, String> queryParams, Map<String, Object> payload);

    // Rotas do Admin (userId explícito)
    List<PaymentResponse> findByUserId(Long userId);
}
