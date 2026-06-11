package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderResponse;
import com.adegadopaibackend.adegadopaibackend.entity.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    // Rotas do usuário (userId extraído do token via SecurityUtils)
    OrderResponse create(CreateOrderRequest req);
    List<OrderResponse> findMyOrders();
    OrderResponse findById(Long orderId);

    // Rotas do Admin
    List<OrderResponse> findAll();
    OrderResponse updateStatus(Long id, OrderStatus status);
}
