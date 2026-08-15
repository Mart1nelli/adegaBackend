package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.config.MercadoPagoProperties;
import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Order;
import com.adegadopaibackend.adegadopaibackend.entity.OrderItem;
import com.adegadopaibackend.adegadopaibackend.entity.Payment;
import com.adegadopaibackend.adegadopaibackend.entity.PaymentMethod;
import com.adegadopaibackend.adegadopaibackend.entity.Product;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.entity.enums.OrderStatus;
import com.adegadopaibackend.adegadopaibackend.entity.enums.PaymentStatus;
import com.adegadopaibackend.adegadopaibackend.exception.BusinessException;
import com.adegadopaibackend.adegadopaibackend.exception.PaymentGatewayException;
import com.adegadopaibackend.adegadopaibackend.mapper.PaymentMapper;
import com.adegadopaibackend.adegadopaibackend.repository.OrderRepository;
import com.adegadopaibackend.adegadopaibackend.repository.PaymentMethodRepository;
import com.adegadopaibackend.adegadopaibackend.repository.PaymentRepository;
import com.adegadopaibackend.adegadopaibackend.repository.ProductRepository;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import com.adegadopaibackend.adegadopaibackend.service.serviceImpl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private SecurityUtils securityUtils;

    private MercadoPagoProperties mercadoPagoProperties;

    private PaymentServiceImpl paymentService;

    private User user;
    private Order order;
    private PaymentMethod paymentMethod;
    private Payment payment;

    @BeforeEach
    void setUp() {
        mercadoPagoProperties = new MercadoPagoProperties(
                "TEST-ACCESS-TOKEN",
                "https://api.adegadopai.com/api/v1/payments/webhook",
                "http://localhost:4200/checkout/sucesso",
                "http://localhost:4200/checkout/falha",
                "http://localhost:4200/checkout/pendente",
                true
        );

        paymentService = new PaymentServiceImpl(
                paymentRepository,
                orderRepository,
                userRepository,
                paymentMethodRepository,
                productRepository,
                paymentMapper,
                securityUtils,
                mercadoPagoProperties
        );

        user = User.builder()
                .id(1L)
                .name("Carlos Silva")
                .email("carlos@example.com")
                .build();

        order = Order.builder()
                .id(10L)
                .user(user)
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        paymentMethod = PaymentMethod.builder()
                .id(1L)
                .name("Mercado Pago")
                .build();

        payment = Payment.builder()
                .id(100L)
                .user(user)
                .order(order)
                .method(paymentMethod)
                .amount(new BigDecimal("150.00"))
                .status(PaymentStatus.PENDING)
                .build();
    }

    @Test
    void create_ShouldThrowException_WhenOrderDoesNotBelongToUser() {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(User.builder().id(2L).build()));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order)); // order belongs to user 1L

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .orderId(10L)
                .paymentMethodId(1L)
                .build();

        assertThrows(BusinessException.class, () -> paymentService.create(req));
    }

    @Test
    void create_ShouldThrowException_WhenOrderIsAlreadyPaid() {
        order.setStatus(OrderStatus.PAID);
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .orderId(10L)
                .paymentMethodId(1L)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.create(req));
        assertTrue(ex.getMessage().contains("já foi pago"));
    }

    @Test
    void create_ShouldThrowException_WhenOrderIsCanceled() {
        order.setStatus(OrderStatus.CANCELED);
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .orderId(10L)
                .paymentMethodId(1L)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.create(req));
        assertTrue(ex.getMessage().contains("cancelado"));
    }

    @Test
    void create_ShouldThrowException_WhenOrderAmountIsZero() {
        order.setTotalAmount(BigDecimal.ZERO);
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .orderId(10L)
                .paymentMethodId(1L)
                .build();

        BusinessException ex = assertThrows(BusinessException.class, () -> paymentService.create(req));
        assertTrue(ex.getMessage().contains("maior que zero"));
    }

    @Test
    void create_ShouldThrowException_WhenAccessTokenNotConfigured() {
        MercadoPagoProperties emptyTokenProps = new MercadoPagoProperties(
                "",
                "http://localhost:8080/api/v1/payments/webhook",
                "http://localhost:4200/checkout/sucesso",
                "http://localhost:4200/checkout/falha",
                "http://localhost:4200/checkout/pendente",
                true
        );

        PaymentServiceImpl serviceWithoutToken = new PaymentServiceImpl(
                paymentRepository,
                orderRepository,
                userRepository,
                paymentMethodRepository,
                productRepository,
                paymentMapper,
                securityUtils,
                emptyTokenProps
        );

        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .orderId(10L)
                .paymentMethodId(1L)
                .build();

        PaymentGatewayException ex = assertThrows(PaymentGatewayException.class, () -> serviceWithoutToken.create(req));
        assertTrue(ex.getMessage().contains("Token de acesso do Mercado Pago não configurado"));
    }

    @Test
    void findById_ShouldReturnPayment_WhenUserIsOwner() {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(securityUtils.isAdmin()).thenReturn(false);
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(PaymentResponse.builder().id(100L).build());

        PaymentResponse response = paymentService.findById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void findById_ShouldThrowAccessDenied_WhenUserIsNotOwnerAndNotAdmin() {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(999L);
        when(securityUtils.isAdmin()).thenReturn(false);
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));

        assertThrows(AccessDeniedException.class, () -> paymentService.findById(100L));
    }

    @Test
    void findById_ShouldReturnPayment_WhenUserIsAdmin() {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(999L);
        when(securityUtils.isAdmin()).thenReturn(true);
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(PaymentResponse.builder().id(100L).build());

        PaymentResponse response = paymentService.findById(100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
    }

    @Test
    void findByOrderId_ShouldReturnPayments_WhenUserIsOwner() {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(securityUtils.isAdmin()).thenReturn(false);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(payment));
        when(paymentMapper.toResponseList(List.of(payment))).thenReturn(List.of(PaymentResponse.builder().id(100L).build()));

        List<PaymentResponse> responses = paymentService.findByOrderId(10L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getId());
    }

    @Test
    void findByOrderId_ShouldThrowAccessDenied_WhenUserIsNotOwner() {
        when(securityUtils.getAuthenticatedUserId()).thenReturn(2L);
        when(securityUtils.isAdmin()).thenReturn(false);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(AccessDeniedException.class, () -> paymentService.findByOrderId(10L));
    }

    @Test
    void webhook_ShouldIgnoreNonPaymentEvents() {
        paymentService.processarNotificacaoWebhook(Map.of("topic", "merchant_order", "id", "12345"), null);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void webhook_ShouldIgnoreWhenNoPaymentId() {
        paymentService.processarNotificacaoWebhook(Map.of("type", "payment"), Map.of());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void syncPaymentStatus_ShouldReturnImmediateResponse_WhenPaymentAlreadyCompleted() {
        payment.setStatus(PaymentStatus.COMPLETED);
        when(securityUtils.getAuthenticatedUserId()).thenReturn(1L);
        when(securityUtils.isAdmin()).thenReturn(false);
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(PaymentResponse.builder().id(100L).status(PaymentStatus.COMPLETED).build());

        PaymentResponse res = paymentService.syncPaymentStatus(100L);

        assertEquals(PaymentStatus.COMPLETED, res.getStatus());
    }
}
