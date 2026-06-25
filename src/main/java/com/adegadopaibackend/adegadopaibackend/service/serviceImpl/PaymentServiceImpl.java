package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Order;
import com.adegadopaibackend.adegadopaibackend.entity.Payment;
import com.adegadopaibackend.adegadopaibackend.entity.PaymentMethod;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.entity.enums.PaymentStatus;
import com.adegadopaibackend.adegadopaibackend.exception.BusinessException;
import com.adegadopaibackend.adegadopaibackend.mapper.PaymentMapper;
import com.adegadopaibackend.adegadopaibackend.repository.OrderRepository;
import com.adegadopaibackend.adegadopaibackend.repository.PaymentMethodRepository;
import com.adegadopaibackend.adegadopaibackend.repository.PaymentRepository;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import com.adegadopaibackend.adegadopaibackend.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public PaymentResponse create(CreatePaymentRequest req) {
        Long userId = securityUtils.getAuthenticatedUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found"));

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order with ID: " + req.getOrderId() + " not found"));

        // Validação de negócio: o pedido deve pertencer ao usuário logado (proteção IDOR de dados)
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("Este pedido não pertence ao usuário logado.");
        }

        PaymentMethod method = paymentMethodRepository.findById(req.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Payment method with ID: " + req.getPaymentMethodId() + " not found"));

        Payment payment = Payment.builder()
                .user(user)
                .order(order)
                .method(method)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        return paymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findMyPayments() {
        Long userId = securityUtils.getAuthenticatedUserId();
        return paymentMapper.toResponseList(paymentRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findByOrderId(Long orderId) {
        return paymentMapper.toResponseList(paymentRepository.findByOrderId(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findByUserId(Long userId) {
        return paymentMapper.toResponseList(paymentRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }
}
