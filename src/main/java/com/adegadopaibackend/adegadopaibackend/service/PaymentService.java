package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    // Rotas do Usuário (userId extraído do token)
    PaymentResponse create(CreatePaymentRequest req);
    List<PaymentResponse> findMyPayments();

    // Rota do Webhook (Mercado Pago)
    void processarNotificacaoWebhook(Long dataId, String type, Map<String, Object> payload);

    // Rotas do Admin (userId explícito)
    List<PaymentResponse> findByOrderId(Long orderId);
    List<PaymentResponse> findByUserId(Long userId);
}
