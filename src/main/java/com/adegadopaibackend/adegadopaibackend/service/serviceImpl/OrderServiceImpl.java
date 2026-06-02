package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderResponse;
import com.adegadopaibackend.adegadopaibackend.entity.*;
import com.adegadopaibackend.adegadopaibackend.entity.enums.OrderStatus;
import com.adegadopaibackend.adegadopaibackend.exception.BusinessException;
import com.adegadopaibackend.adegadopaibackend.mapper.OrderMapper;
import com.adegadopaibackend.adegadopaibackend.repository.*;
import com.adegadopaibackend.adegadopaibackend.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(Long userId, CreateOrderRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Address address = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        if (!address.getUser().getId().equals(userId)) {
            throw new BusinessException("Este endereço não pertence ao usuário logado.");
        }

        Cart cart = cartRepository.findTopByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new BusinessException("Nenhum carrinho ativo encontrado."));

        if (cart.getCartItems().isEmpty()) {
            throw new BusinessException("O carrinho está vazio.");
        }

        Order order = Order.builder()
                .user(user)
                .address(address)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new BusinessException("Estoque insuficiente para o produto: " + product.getName());
            }

            product.setStock(product.getStock() - cartItem.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.getOrderItems().add(orderItem);

            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);
        }

        order.setTotalAmount(total);

        cart.getCartItems().clear();
        cartRepository.save(cart);

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Override
    public List<OrderResponse> findAllByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with ID: " + userId + " not found");
        }
        return orderMapper.toResponseList(orderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order with ID: " + orderId + " not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("This request belongs to another user.");
        }

        return orderMapper.toResponse(order);
    }

    @Override
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order with ID: " + id + " not found"));

        OrderStatus currentStatus = order.getStatus();

        validateStatusTransition(currentStatus, status);

        if (status == OrderStatus.CANCELED  && currentStatus != OrderStatus.CANCELED) {
            returnOrderItemsToStock(order);
        }

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toResponse(updatedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderMapper.toResponseList(orderRepository.findAll());
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {

        if (current == OrderStatus.CANCELED || current == OrderStatus.DELIVERED) {
            throw new BusinessException("It is not possible to change the status of an order that has already been " + current);
        }

        if (current == OrderStatus.PAID && next == OrderStatus.PENDING) {
            throw new BusinessException("A paid order cannot revert to Pending.");
        }

        if (current == OrderStatus.SHIPPED && (next == OrderStatus.PAID || next == OrderStatus.PENDING)) {
            throw new BusinessException("The order is already en route and cannot be reversed.");
        }
    }

    private void returnOrderItemsToStock(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
        }
    }
}
