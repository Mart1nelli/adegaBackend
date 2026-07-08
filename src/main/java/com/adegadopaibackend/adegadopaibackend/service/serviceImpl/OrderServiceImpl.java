package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderResponse;
import com.adegadopaibackend.adegadopaibackend.entity.*;
import com.adegadopaibackend.adegadopaibackend.entity.enums.OrderStatus;
import com.adegadopaibackend.adegadopaibackend.exception.BusinessException;
import com.adegadopaibackend.adegadopaibackend.mapper.OrderMapper;
import com.adegadopaibackend.adegadopaibackend.repository.*;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        Long userId = securityUtils.getAuthenticatedUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Address address = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));

        // Validação de negócio: o endereço deve pertencer ao usuário logado
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
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(total);
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findMyOrders() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return orderMapper.toResponseList(orderRepository.findByUserIdWithItems(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(Long orderId) {
        // A segurança (isOwner ou ADMIN) é garantida pelo @PreAuthorize no Controller
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order with ID: " + orderId + " not found"));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderMapper.toResponseList(orderRepository.findAll());
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order with ID: " + id + " not found"));

        validateStatusTransition(order.getStatus(), status);

        if (status == OrderStatus.CANCELED && order.getStatus() != OrderStatus.CANCELED) {
            returnOrderItemsToStock(order);
        }

        order.setStatus(status);
        return orderMapper.toResponse(orderRepository.save(order));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELED || current == OrderStatus.DELIVERED) {
            throw new BusinessException("Não é possível alterar o status de um pedido que já está " + current);
        }
        if (current == OrderStatus.PAID && next == OrderStatus.PENDING) {
            throw new BusinessException("Um pedido pago não pode voltar para Pendente.");
        }
        if (current == OrderStatus.SHIPPED && (next == OrderStatus.PAID || next == OrderStatus.PENDING)) {
            throw new BusinessException("O pedido já está a caminho e não pode ser revertido.");
        }
    }

    private void returnOrderItemsToStock(Order order) {
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStock(product.getStock() + orderItem.getQuantity());
        }
    }
}
