package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Order;
import com.adegadopaibackend.adegadopaibackend.entity.enums.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse create(Long userId, CreateOrderRequest req);

    List<OrderResponse> findAllByUserId(Long userId);

    OrderResponse findById(Long userId, Long orderId);

    OrderResponse updateStatus(Long id, OrderStatus status);

    List<OrderResponse> findAll();
}
