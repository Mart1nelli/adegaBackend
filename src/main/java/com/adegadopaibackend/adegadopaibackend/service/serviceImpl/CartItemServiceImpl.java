package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateCartItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateCartItemRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.CartItemResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Cart;
import com.adegadopaibackend.adegadopaibackend.entity.CartItem;
import com.adegadopaibackend.adegadopaibackend.entity.Product;
import com.adegadopaibackend.adegadopaibackend.mapper.CartItemMapper;
import com.adegadopaibackend.adegadopaibackend.repository.CartItemRepository;
import com.adegadopaibackend.adegadopaibackend.repository.CartRepository;
import com.adegadopaibackend.adegadopaibackend.repository.ProductRepository;
import com.adegadopaibackend.adegadopaibackend.service.CartItemService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    public CartItemResponse create(CreateCartItemRequest req) {
        CartItem cartItem = cartItemMapper.toEntity(req);

        Cart cart = cartRepository.findById(req.getCartId())
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        cartItem.setCart(cart);
        cartItem.setProduct(product);

        CartItem savedCartItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toResponse(savedCartItem);
    }

    @Override
    public List<CartItemResponse> findAll() {
        List<CartItem> cartItems = cartItemRepository.findAll();
        return cartItems.stream()
                .map(cartItemMapper::toResponse)
                .toList();
    }

    @Override
    public CartItemResponse findById(Long id) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

        return cartItemMapper.toResponse(cartItem);
    }

    @Override
    public CartItemResponse update(Long id, UpdateCartItemRequest req) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

        cartItemMapper.updateEntity(req, cartItem);

        CartItem updatedCartItem = cartItemRepository.save(cartItem);
        return cartItemMapper.toResponse(updatedCartItem);
    }

    @Override
    public void delete(Long id) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

        cartItemRepository.delete(cartItem);
    }
}
