package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.AddToCartRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.CartResponse;
import com.adegadopaibackend.adegadopaibackend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/carts")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getOrCreateCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(cartService.addItem(req));
    }

    @PreAuthorize("@cartSecurity.isOwner(#cartItemId)")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItem(@PathVariable Long cartItemId,
                                                   @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItem(cartItemId, quantity));
    }

    @PreAuthorize("@cartSecurity.isOwner(#cartItemId)")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItem(cartItemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }

    // --- MÉTODOS DO ADMIN ---
    // Rotas protegidas exclusivamente para quem tem a Role ADMIN

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{userId}")
    public ResponseEntity<CartResponse> getCartForAdmin(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getOrCreateCartForAdmin(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{userId}/items")
    public ResponseEntity<CartResponse> addItemAdmin(@PathVariable Long userId,
                                                     @Valid @RequestBody AddToCartRequest req) {
        return ResponseEntity.ok(cartService.addItemForAdmin(userId, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/{userId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateItemAdmin(@PathVariable Long userId,
                                                        @PathVariable Long cartItemId,
                                                        @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItem(cartItemId, quantity));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{userId}/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItemAdmin(@PathVariable Long userId,
                                                        @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItem(cartItemId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<Void> clearCartAdmin(@PathVariable Long userId) {
        cartService.clearCartForAdmin(userId);
        return ResponseEntity.noContent().build();
    }
}