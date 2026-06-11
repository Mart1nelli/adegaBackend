package com.adegadopaibackend.adegadopaibackend.service.security;

import com.adegadopaibackend.adegadopaibackend.repository.OrderRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("orderSecurity")
@RequiredArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;
    private final SecurityUtils securityUtils;

    public boolean isOwner(Long orderId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return orderRepository.findById(orderId)
                .map(order -> order.getUser().getId().equals(userId))
                .orElse(false);
    }
}
