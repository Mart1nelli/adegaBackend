package com.adegadopaibackend.adegadopaibackend.controller;

import java.util.List;

import com.adegadopaibackend.adegadopaibackend.dto.response.CartItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateCartItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateCartItemRequest;
import com.adegadopaibackend.adegadopaibackend.service.CartItemService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;

    @PostMapping
    public ResponseEntity<CartItemResponse> create(@Valid @RequestBody CreateCartItemRequest req) {
        CartItemResponse res = cartItemService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CartItemResponse>> findAll() {
        return ResponseEntity.ok(cartItemService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@cartSecurity.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<CartItemResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(cartItemService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@cartSecurity.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<CartItemResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateCartItemRequest req) {
        return ResponseEntity.ok(cartItemService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@cartSecurity.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cartItemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
