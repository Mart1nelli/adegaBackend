package com.adegadopaibackend.adegadopaibackend.service.security;

import com.adegadopaibackend.adegadopaibackend.repository.CartItemRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

@Component("cartSecurity")
@RequiredArgsConstructor
public class CartSecurity {
    private final CartItemRepository cartItemRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public boolean isOwner(Long cartItemId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return cartItemRepository.findById(cartItemId)
                .map(item -> item.getCart().getUser().getId().equals(userId))
                .orElse(false);
    }
}
