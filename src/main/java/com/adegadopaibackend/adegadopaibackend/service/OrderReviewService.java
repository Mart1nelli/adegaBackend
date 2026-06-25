package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderReviewRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderReviewResponse;

import java.util.List;

public interface OrderReviewService {

    // Rotas do Usuário (userId extraído do token)
    OrderReviewResponse create(Long orderId, CreateOrderReviewRequest req);
    List<OrderReviewResponse> findMyOrderReviews();

    // Rotas públicas / Admin
    List<OrderReviewResponse> findByOrderId(Long orderId);
    List<OrderReviewResponse> findByUserId(Long userId);
}
