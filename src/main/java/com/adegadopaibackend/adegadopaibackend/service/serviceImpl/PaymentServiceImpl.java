package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adegadopaibackend.adegadopaibackend.config.MercadoPagoProperties;
import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.CreateTransparentPaymentRequest;
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
import com.adegadopaibackend.adegadopaibackend.service.PaymentService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPSearchRequest;
import com.mercadopago.resources.preference.Preference;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ProductRepository productRepository;
    private final PaymentMapper paymentMapper;
    private final SecurityUtils securityUtils;
    private final MercadoPagoProperties mercadoPagoProperties;

    @Override
    @Transactional
    public PaymentResponse create(CreatePaymentRequest req) {
        Long userId = securityUtils.getAuthenticatedUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found"));

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order with ID: " + req.getOrderId() + " not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("Este pedido não pertence ao usuário logado.");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException("Este pedido já foi pago.");
        }

        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException("Não é possível realizar pagamento para um pedido cancelado.");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Este pedido já foi finalizado.");
        }

        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do pedido deve ser maior que zero.");
        }

        PaymentMethod method = paymentMethodRepository.findById(req.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found"));

        if (!hasText(mercadoPagoProperties.accessToken())) {
            throw new PaymentGatewayException("Token de acesso do Mercado Pago não configurado. Defina a variável MERCADOPAGO_ACCESS_TOKEN no arquivo .env.");
        }

        MercadoPagoConfig.setAccessToken(mercadoPagoProperties.accessToken().trim());

        Payment payment = Payment.builder()
                .user(user)
                .order(order)
                .method(method)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        try {
            PreferenceClient preferenceClient = new PreferenceClient();

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(String.valueOf(order.getId()))
                    .title("Pedido #" + order.getId() + " - Adega do Pai")
                    .quantity(1)
                    .unitPrice(order.getTotalAmount())
                    .currencyId("BRL")
                    .build();

            PreferencePayerRequest.PreferencePayerRequestBuilder payerBuilder = PreferencePayerRequest.builder();
            if (hasText(user.getEmail())) {
                payerBuilder.email(user.getEmail().trim());
            }
            if (hasText(user.getName())) {
                String name = user.getName().trim();
                String[] nameParts = name.split("\\s+", 2);
                payerBuilder.name(nameParts[0]);
                if (nameParts.length > 1) {
                    payerBuilder.surname(nameParts[1]);
                }
            }

            PreferenceBackUrlsRequest.PreferenceBackUrlsRequestBuilder backUrlsBuilder = PreferenceBackUrlsRequest.builder();
            boolean hasSuccessUrl = hasText(mercadoPagoProperties.frontendSuccessUrl());
            if (hasSuccessUrl) {
                backUrlsBuilder.success(mercadoPagoProperties.frontendSuccessUrl().trim());
            }
            if (hasText(mercadoPagoProperties.frontendFailureUrl())) {
                backUrlsBuilder.failure(mercadoPagoProperties.frontendFailureUrl().trim());
            }
            if (hasText(mercadoPagoProperties.frontendPendingUrl())) {
                backUrlsBuilder.pending(mercadoPagoProperties.frontendPendingUrl().trim());
            }

            PreferenceRequest.PreferenceRequestBuilder preferenceRequestBuilder = PreferenceRequest.builder()
                    .items(List.of(itemRequest))
                    .payer(payerBuilder.build())
                    .backUrls(backUrlsBuilder.build())
                    .externalReference(payment.getId().toString());

            if (hasSuccessUrl) {
                preferenceRequestBuilder.autoReturn("approved");
            }

            String webhookUrl = mercadoPagoProperties.webhookUrl();
            if (isPublicWebhookUrl(webhookUrl)) {
                preferenceRequestBuilder.notificationUrl(webhookUrl.trim());
            } else {
                log.info("Webhook URL '{}' é local ou vazia; notificationUrl omitida na preferência do Mercado Pago.", webhookUrl);
            }

            Preference preference = preferenceClient.create(preferenceRequestBuilder.build());

            payment.setTransactionId(preference.getId());
            payment = paymentRepository.save(payment);

            PaymentResponse response = paymentMapper.toResponse(payment);
            response.setCheckoutUrl(mercadoPagoProperties.resolveCheckoutUrl(preference));
            return response;
        } catch (MPApiException ex) {
            String responseBody = ex.getApiResponse() != null ? ex.getApiResponse().getContent() : "Sem detalhes";
            int statusCode = ex.getStatusCode();
            log.error("Falha ao criar preferência no Mercado Pago para pedido {}. HTTP {}: {}", order.getId(), statusCode, responseBody, ex);
            throw new PaymentGatewayException("Falha ao criar checkout do Mercado Pago: " + responseBody, ex);
        } catch (MPException ex) {
            log.error("Erro no SDK do Mercado Pago ao criar checkout para pedido {}", order.getId(), ex);
            throw new PaymentGatewayException("Erro ao comunicar com o Mercado Pago: " + ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional
    public PaymentResponse createTransparent(CreateTransparentPaymentRequest req) {
        Long userId = securityUtils.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found"));
        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order with ID: " + req.getOrderId() + " not found"));

        validateOrderForPayment(order, userId);
        PaymentMethod method = paymentMethodRepository.findById(req.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found"));

        if (!hasText(mercadoPagoProperties.accessToken())) {
            throw new PaymentGatewayException("Token de acesso do Mercado Pago não configurado.");
        }

        MercadoPagoConfig.setAccessToken(mercadoPagoProperties.accessToken().trim());
        Payment payment = paymentRepository.save(Payment.builder()
                .user(user)
                .order(order)
                .method(method)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build());

        try {
            PaymentCreateRequest.PaymentCreateRequestBuilder requestBuilder = PaymentCreateRequest.builder()
                    .transactionAmount(order.getTotalAmount())
                    .description("Pedido #" + order.getId() + " - Adega do Pai")
                    .paymentMethodId(req.getMercadoPagoPaymentMethodId().trim())
                    .installments(req.getInstallments() == null ? 1 : req.getInstallments())
                    .externalReference(payment.getId().toString());

            if (isPublicWebhookUrl(mercadoPagoProperties.webhookUrl())) {
                requestBuilder.notificationUrl(mercadoPagoProperties.webhookUrl().trim());
            }

            if (hasText(req.getToken())) {
                requestBuilder.token(req.getToken().trim());
            }
            if (hasText(req.getIssuerId())) {
                requestBuilder.issuerId(req.getIssuerId().trim());
            }

            String payerEmail = hasText(req.getPayerEmail()) ? req.getPayerEmail().trim() : user.getEmail();
            if (hasText(payerEmail)) {
                requestBuilder.payer(PaymentPayerRequest.builder().email(payerEmail.trim()).build());
            }

            com.mercadopago.resources.payment.Payment mpPayment = new PaymentClient().create(requestBuilder.build());
            if (mpPayment == null || mpPayment.getId() == null) {
                throw new PaymentGatewayException("O Mercado Pago não retornou um pagamento válido.");
            }

            syncLocalPaymentWithGateway(payment, mpPayment);
            return paymentMapper.toResponse(payment);
        } catch (MPApiException ex) {
            String responseBody = ex.getApiResponse() != null ? ex.getApiResponse().getContent() : "Sem detalhes";
            log.error("Falha ao criar pagamento transparente para pedido {}. HTTP {}: {}", order.getId(), ex.getStatusCode(), responseBody, ex);
            throw new PaymentGatewayException("Falha ao criar pagamento no Mercado Pago: " + responseBody, ex);
        } catch (MPException ex) {
            log.error("Erro no SDK do Mercado Pago ao criar pagamento transparente para pedido {}", order.getId(), ex);
            throw new PaymentGatewayException("Erro ao comunicar com o Mercado Pago: " + ex.getMessage(), ex);
        }
    }

    private void validateOrderForPayment(Order order, Long userId) {
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("Este pedido não pertence ao usuário logado.");
        }
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException("Este pedido já foi pago.");
        }
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new BusinessException("Não é possível realizar pagamento para um pedido cancelado.");
        }
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Este pedido já foi finalizado.");
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do pedido deve ser maior que zero.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findById(Long id) {
        Long userId = securityUtils.getAuthenticatedUserId();
        boolean isAdmin = securityUtils.isAdmin();

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento com ID: " + id + " não encontrado."));

        if (!isAdmin && !payment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Acesso negado: este pagamento não pertence ao usuário logado.");
        }

        return paymentMapper.toResponse(payment);
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
        Long userId = securityUtils.getAuthenticatedUserId();
        boolean isAdmin = securityUtils.isAdmin();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido com ID: " + orderId + " não encontrado."));

        if (!isAdmin && !order.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Acesso negado: este pedido não pertence ao usuário logado.");
        }

        return paymentMapper.toResponseList(paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> findByUserId(Long userId) {
        return paymentMapper.toResponseList(paymentRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    @Transactional
    public PaymentResponse syncPaymentStatus(Long id) {
        Long userId = securityUtils.getAuthenticatedUserId();
        boolean isAdmin = securityUtils.isAdmin();

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento com ID: " + id + " não encontrado."));

        if (!isAdmin && !payment.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Acesso negado: este pagamento não pertence ao usuário logado.");
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return paymentMapper.toResponse(payment);
        }

        if (!hasText(mercadoPagoProperties.accessToken())) {
            return paymentMapper.toResponse(payment);
        }

        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoProperties.accessToken().trim());
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = null;

            if (hasText(payment.getTransactionId())) {
                try {
                    Long mpPaymentId = Long.parseLong(payment.getTransactionId().trim());
                    mpPayment = client.get(mpPaymentId);
                } catch (NumberFormatException ignored) {
                    // transactionId might be preferenceId
                } catch (MPApiException ex) {
                    if (ex.getStatusCode() != 404) {
                        log.warn("Erro ao buscar pagamento {} por ID no Mercado Pago: HTTP {}", payment.getTransactionId(), ex.getStatusCode());
                    }
                }
            }

            if (mpPayment == null) {
                try {
                    var searchResults = client.search(MPSearchRequest.builder()
                            .offset(0)
                            .limit(1)
                            .filters(Map.of("external_reference", payment.getId().toString()))
                            .build());
                    if (searchResults != null && searchResults.getResults() != null && !searchResults.getResults().isEmpty()) {
                        mpPayment = searchResults.getResults().get(0);
                    }
                } catch (Exception ex) {
                    log.warn("Falha na busca de pagamento do Mercado Pago por external_reference {}: {}", payment.getId(), ex.getMessage());
                }
            }

            if (mpPayment != null) {
                syncLocalPaymentWithGateway(payment, mpPayment);
            }
        } catch (Exception ex) {
            log.error("Erro ao sincronizar status do pagamento {} com Mercado Pago", id, ex);
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void processarNotificacaoWebhook(Map<String, String> queryParams, Map<String, Object> payload) {
        Map<String, String> safeQueryParams = queryParams == null ? Map.of() : queryParams;
        Map<String, Object> safePayload = payload == null ? Map.of() : payload;

        Long mpPaymentId = resolveMercadoPagoPaymentId(safeQueryParams, safePayload);
        String eventType = resolveEventType(safeQueryParams, safePayload);

        log.info("Recebido webhook Mercado Pago. Tipo: {}, ID Pagamento: {}", eventType, mpPaymentId);

        if (!isPaymentEvent(eventType)) {
            log.debug("Ignorando evento webhook de tipo não suportado: {}", eventType);
            return;
        }

        if (mpPaymentId == null) {
            log.warn("Webhook do Mercado Pago recebido sem identificador de pagamento (data.id/id).");
            return;
        }

        if (!hasText(mercadoPagoProperties.accessToken())) {
            log.error("Webhook recebido, mas MERCADOPAGO_ACCESS_TOKEN não está configurado.");
            return;
        }

        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoProperties.accessToken().trim());
            PaymentClient client = new PaymentClient();
            com.mercadopago.resources.payment.Payment mpPayment = client.get(mpPaymentId);

            if (mpPayment == null) {
                log.warn("Pagamento Mercado Pago {} retornou nulo da API.", mpPaymentId);
                return;
            }

            String externalReference = mpPayment.getExternalReference();
            Payment localPayment = null;

            if (hasText(externalReference)) {
                try {
                    Long localPaymentId = Long.parseLong(externalReference.trim());
                    localPayment = paymentRepository.findById(localPaymentId).orElse(null);
                } catch (NumberFormatException ex) {
                    log.warn("Referência externa do Mercado Pago não é numérica: {}", externalReference);
                }
            }

            if (localPayment == null && mpPayment.getId() != null) {
                localPayment = paymentRepository.findByTransactionId(mpPayment.getId().toString()).orElse(null);
            }

            if (localPayment == null) {
                log.warn("Pagamento local não encontrado para evento MP {}. External reference: {}", mpPaymentId, externalReference);
                return;
            }

            syncLocalPaymentWithGateway(localPayment, mpPayment);
        } catch (MPApiException ex) {
            String content = ex.getApiResponse() != null ? ex.getApiResponse().getContent() : "Sem conteúdo";
            log.error("Erro da API Mercado Pago no processamento do webhook {}. HTTP {}: {}", mpPaymentId, ex.getStatusCode(), content, ex);
            if (ex.getStatusCode() != 404) {
                throw new PaymentGatewayException("Falha ao consultar pagamento no Mercado Pago: " + content, ex);
            }
        } catch (MPException ex) {
            log.error("Erro no SDK Mercado Pago durante processamento do webhook {}", mpPaymentId, ex);
            throw new PaymentGatewayException("Erro de comunicação com o Mercado Pago.", ex);
        }
    }

    private void syncLocalPaymentWithGateway(Payment localPayment, com.mercadopago.resources.payment.Payment mpPayment) {
        String normalizedStatus = normalize(mpPayment.getStatus());
        Order order = localPayment.getOrder();

        if (mpPayment.getId() != null) {
            localPayment.setTransactionId(mpPayment.getId().toString());
        }

        switch (normalizedStatus) {
            case "approved" -> {
                localPayment.setStatus(PaymentStatus.COMPLETED);
                if (order != null && order.getStatus() != OrderStatus.PAID) {
                    order.setStatus(OrderStatus.PAID);
                    orderRepository.save(order);
                }
                paymentRepository.save(localPayment);
                log.info("Pagamento {} aprovado com sucesso. Pedido {} marcado como PAID.",
                        localPayment.getId(), order != null ? order.getId() : "null");
            }
            case "refunded", "charged_back" -> {
                localPayment.setStatus(PaymentStatus.REFUNDED);
                if (order != null) {
                    cancelOrderAndRestoreStock(order);
                    orderRepository.save(order);
                }
                paymentRepository.save(localPayment);
                log.info("Pagamento {} estornado/devolvido. Pedido {} cancelado e estoque restaurado.",
                        localPayment.getId(), order != null ? order.getId() : "null");
            }
            case "rejected", "cancelled" -> {
                localPayment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(localPayment);
                log.info("Pagamento {} falhou no gateway com status {}.", localPayment.getId(), normalizedStatus);
            }
            case "pending", "in_process", "authorized" -> {
                localPayment.setStatus(PaymentStatus.PENDING);
                paymentRepository.save(localPayment);
                log.info("Pagamento {} permanece pendente com status {}.", localPayment.getId(), normalizedStatus);
            }
            default -> {
                log.info("Status '{}' do Mercado Pago recebido e não mapeado para o pagamento {}.",
                        normalizedStatus, localPayment.getId());
            }
        }
    }

    private void cancelOrderAndRestoreStock(Order order) {
        if (order.getStatus() == OrderStatus.CANCELED
                || order.getStatus() == OrderStatus.SHIPPED
                || order.getStatus() == OrderStatus.DELIVERED) {
            return;
        }

        restoreOrderStock(order);
        order.setStatus(OrderStatus.CANCELED);
    }

    private void restoreOrderStock(Order order) {
        if (order.getOrderItems() != null) {
            for (OrderItem orderItem : order.getOrderItems()) {
                if (orderItem.getProduct() != null) {
                    Product product = orderItem.getProduct();
                    product.setStock(product.getStock() + orderItem.getQuantity());
                    productRepository.save(product);
                }
            }
        }
    }

    private Long resolveMercadoPagoPaymentId(Map<String, String> queryParams, Map<String, Object> payload) {
        String rawId = firstNonBlank(
                queryParams.get("data.id"),
                queryParams.get("data_id"),
                queryParams.get("id"),
                queryParams.get("payment_id"),
                extractPayloadId(payload)
        );

        if (!hasText(rawId)) {
            return null;
        }

        try {
            return Long.parseLong(rawId);
        } catch (NumberFormatException ex) {
            throw new PaymentGatewayException("Webhook do Mercado Pago com identificador inválido.", ex);
        }
    }

    private String resolveEventType(Map<String, String> queryParams, Map<String, Object> payload) {
        String type = firstNonBlank(
                queryParams.get("type"),
                queryParams.get("topic"),
                queryParams.get("action"),
                extractPayloadString(payload, "type"),
                extractPayloadString(payload, "topic"),
                extractPayloadString(payload, "action")
        );
        return type == null ? null : type.toLowerCase(Locale.ROOT);
    }

    private boolean isPaymentEvent(String eventType) {
        return eventType == null || eventType.isBlank() || "payment".equals(eventType) || "payments".equals(eventType)
                || eventType.startsWith("payment.");
    }

    private String extractPayloadId(Map<String, Object> payload) {
        Object data = payload != null ? payload.get("data") : null;
        if (data instanceof Map<?, ?> dataMap) {
            Object id = dataMap.get("id");
            return id != null ? id.toString() : null;
        }

        return extractPayloadString(payload, "data.id");
    }

    private String extractPayloadString(Map<String, Object> payload, String key) {
        if (payload == null || !payload.containsKey(key)) {
            return null;
        }

        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean isPublicWebhookUrl(String url) {
        if (!hasText(url)) {
            return false;
        }
        String lower = url.trim().toLowerCase(Locale.ROOT);
        return (lower.startsWith("https://") || lower.startsWith("http://"))
                && !lower.contains("localhost")
                && !lower.contains("127.0.0.1")
                && !lower.contains("::1")
                && !lower.contains(".local");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
