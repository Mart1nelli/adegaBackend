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

    @Override
    public CartResponse getOrCreateCart(Long userId) {
        Cart cart = cartRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseGet(() -> createCartForUser(userId));
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddToCartRequest req) {
        Cart cart = cartRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseGet(() -> createCartForUser(userId));

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product with ID: " + req.getProductId() + " not found"));

        if (product.getStock() < req.getQuantity()) {
            throw new BusinessException("Stock is not enough for this product: " + product.getName() + ". Available: " + product.getStock());
        }

        cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> {
                            int newQuantity = item.getQuantity() + req.getQuantity();
                            if (product.getStock() < newQuantity) {
                                throw new BusinessException("Insufficient stock. You already have " +
                                        item.getQuantity() + " In your cart and tried to add more " + req.getQuantity());
                            }
                            item.setQuantity(newQuantity);
                        },
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .quantity(req.getQuantity())
                                    .cart(cart)
                                    .product(product)
                                    .build();
                            cart.getCartItems().add(newItem);
                        }
                );

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item with ID: " + cartItemId + " not found"));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new BusinessException("You are not authorized to update this cart item");
        }

        if (cartItem.getProduct().getStock() < quantity) {
            throw new BusinessException("Insufficient stock for this product");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("Cart item with ID: " + cartItemId + " not found"));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new BusinessException("You are not authorized to remove this cart item");
        }

        Cart cart = cartItem.getCart();
        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .ifPresent(cart -> {
                    cart.getCartItems().clear();
                    cartRepository.save(cart);
                });
    }

    private Cart createCartForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found"));
        Cart cart = Cart.builder().user(user).build();
        return cartRepository.save(cart);
    }
}
