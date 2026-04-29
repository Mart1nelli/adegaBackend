package com.adegadopaibackend.adegadopaibackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.adegadopaibackend.adegadopaibackend.dto.request.AddToCartRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.CartResponse;
import com.adegadopaibackend.adegadopaibackend.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
public class CartController {

    private final CartService cartService;


    @GetMapping("/user/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/user/{userId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long userId,
                                                @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(cartService.addItem(userId, req));
    }

    @PutMapping("/user/{userId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long userId,
                                                   @PathVariable Long cartItemId,
                                                   @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItem(userId, cartItemId, quantity));
    }

    @DeleteMapping("/user/{userId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long userId,
                                                   @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, cartItemId));
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
