package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateOrderReviewRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.OrderReviewResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Order;
import com.adegadopaibackend.adegadopaibackend.entity.OrderReview;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.exception.BusinessException;
import com.adegadopaibackend.adegadopaibackend.mapper.OrderReviewMapper;
import com.adegadopaibackend.adegadopaibackend.repository.OrderRepository;
import com.adegadopaibackend.adegadopaibackend.repository.OrderReviewRepository;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import com.adegadopaibackend.adegadopaibackend.service.OrderReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderReviewServiceImpl implements OrderReviewService {

    private final OrderReviewRepository orderReviewRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderReviewMapper orderReviewMapper;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public OrderReviewResponse create(Long orderId, CreateOrderReviewRequest req) {
        Long userId = securityUtils.getAuthenticatedUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order with ID: " + orderId + " not found"));

        // Validação de negócio: apenas o dono do pedido pode avaliá-lo
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("Você não pode avaliar um pedido que não é seu.");
        }

        OrderReview orderReview = orderReviewMapper.toEntity(req);
        orderReview.setUser(user);
        orderReview.setOrder(order);

        return orderReviewMapper.toResponse(orderReviewRepository.save(orderReview));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderReviewResponse> findMyOrderReviews() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return orderReviewMapper.toResponseList(
                orderReviewRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderReviewResponse> findByOrderId(Long orderId) {
        return orderReviewMapper.toResponseList(
                orderReviewRepository.findByOrderIdOrderByCreatedAtDesc(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderReviewResponse> findByUserId(Long userId) {
        return orderReviewMapper.toResponseList(
                orderReviewRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }
}
