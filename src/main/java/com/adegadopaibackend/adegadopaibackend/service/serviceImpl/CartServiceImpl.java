package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.AddToCartRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.CartResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Cart;
import com.adegadopaibackend.adegadopaibackend.entity.CartItem;
import com.adegadopaibackend.adegadopaibackend.entity.Product;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.exception.BusinessException;
import com.adegadopaibackend.adegadopaibackend.mapper.CartMapper;
import com.adegadopaibackend.adegadopaibackend.repository.CartItemRepository;
import com.adegadopaibackend.adegadopaibackend.repository.CartRepository;
import com.adegadopaibackend.adegadopaibackend.repository.ProductRepository;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import com.adegadopaibackend.adegadopaibackend.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;
    private final SecurityUtils securityUtils;

    // --- MÉTODOS PÚBLICOS (Usuário) ---
    @Override @Transactional
    public CartResponse getOrCreateCart() {
        return getOrCreateCartInternal(securityUtils.getAuthenticatedUserId());
    }

    @Override @Transactional
    public CartResponse addItem(AddToCartRequest req) {
        return addItemInternal(securityUtils.getAuthenticatedUserId(), req);
    }

    @Override @Transactional
    public CartResponse updateItem(Long cartItemId, Integer quantity) {
        // A segurança está no Controller via @PreAuthorize
        return updateItemInternal(cartItemId, quantity);
    }

    @Override @Transactional
    public CartResponse removeItem(Long cartItemId) {
        // A segurança está no Controller via @PreAuthorize
        return removeItemInternal(cartItemId);
    }

    @Override @Transactional
    public void clearCart() {
        clearCartInternal(securityUtils.getAuthenticatedUserId());
    }

    // --- MÉTODOS PÚBLICOS (Admin) ---
    @Override @Transactional
    public CartResponse getOrCreateCartForAdmin(Long userId) {
        return getOrCreateCartInternal(userId);
    }

    @Override @Transactional
    public CartResponse addItemForAdmin(Long userId, AddToCartRequest req) {
        return addItemInternal(userId, req);
    }

    @Override @Transactional
    public void clearCartForAdmin(Long userId) {
        clearCartInternal(userId);
    }

    // --- LÓGICA INTERNA (Focada apenas no Negócio) ---

    private CartResponse getOrCreateCartInternal(Long userId) {
        Cart cart = cartRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseGet(() -> createCartForUser(userId));
        return cartMapper.toResponse(cart);
    }

    private CartResponse addItemInternal(Long userId, AddToCartRequest req) {
        Cart cart = cartRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseGet(() -> createCartForUser(userId));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (product.getStock() < req.getQuantity()) {
            throw new BusinessException("Stock insufficient");
        }

        cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            int newQuantity = item.getQuantity() + req.getQuantity();
                            if (product.getStock() < newQuantity) throw new BusinessException("Insufficient stock.");
                            item.setQuantity(newQuantity);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder().quantity(req.getQuantity()).cart(cart).product(product).build();
                            cart.getCartItems().add(newItem);
                        }
                );
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    private CartResponse updateItemInternal(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        if (cartItem.getProduct().getStock() < quantity) {
            throw new BusinessException("Insufficient stock");
        }

        cartItem.setQuantity(quantity);
        return cartMapper.toResponse(cartItemRepository.save(cartItem).getCart());
    }

    private CartResponse removeItemInternal(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item not found"));

        Cart cart = cartItem.getCart();
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        return cartMapper.toResponse(cartRepository.save(cart));
    }

    private void clearCartInternal(Long userId) {
        cartRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .ifPresent(cart -> {
                    cart.getCartItems().clear();
                    cartRepository.save(cart);
                });
    }

    private Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return cartRepository.save(Cart.builder().user(user).build());
    }
}