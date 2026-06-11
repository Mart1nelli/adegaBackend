package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.AddToCartRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.CartResponse;

public interface CartService {

    // Métodos para o Usuário (segurança via Token)
    CartResponse getOrCreateCart();

    CartResponse addItem(AddToCartRequest req);

    CartResponse updateItem(Long cartItemId, Integer quantity);

    CartResponse removeItem(Long cartItemId);

    void clearCart();

    // Métodos para o Admin (segurança via @PreAuthorize + ID na URL)
    CartResponse getOrCreateCartForAdmin(Long userId);

    CartResponse addItemForAdmin(Long userId, AddToCartRequest req);

    void clearCartForAdmin(Long userId);
}
