package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Order;
import com.adegadopaibackend.adegadopaibackend.entity.Payment;
import com.adegadopaibackend.adegadopaibackend.entity.PaymentMethod;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.entity.enums.OrderStatus;
import com.adegadopaibackend.adegadopaibackend.entity.enums.PaymentStatus;
import com.adegadopaibackend.adegadopaibackend.exception.BusinessException;
import com.adegadopaibackend.adegadopaibackend.mapper.PaymentMapper;
import com.adegadopaibackend.adegadopaibackend.repository.OrderRepository;
import com.adegadopaibackend.adegadopaibackend.repository.PaymentMethodRepository;
import com.adegadopaibackend.adegadopaibackend.repository.PaymentRepository;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import com.adegadopaibackend.adegadopaibackend.service.PaymentService;

import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException("Este pedido não pertence ao usuário logado.");
        }

        PaymentMethod method = paymentMethodRepository.findById(req.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found"));

        // 1. Salva o registro inicial no banco para gerar o ID local do pagamento
        Payment payment = Payment.builder()
                .user(user)
                .order(order)
                .method(method)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        try {
            // 2. Integração com a API de Preferences do Mercado Pago
            PreferenceClient preferenceClient = new PreferenceClient();
            List<PreferenceItemRequest> items = new ArrayList<>();

            items.add(PreferenceItemRequest.builder()
                    .id(order.getId().toString())
                    .title("Pedido #" + order.getId() + " - Adega do Pai")
                    .quantity(1)
                    .unitPrice(order.getTotalAmount())
                    .currencyId("BRL")
                    .build());

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:4200/checkout/sucesso")
                    .failure("http://localhost:4200/checkout/falha")
                    .pending("http://localhost:4200/checkout/pendente")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .externalReference(payment.getId().toString())
                    .notificationUrl("https://grinning-bribe-unwired.ngrok-free.dev/api/v1/payments/webhook")
                    .build();

            Preference preference = preferenceClient.create(preferenceRequest);

            // 3. Atualiza o pagamento com o ID da preferência gerada
            payment.setTransactionId(preference.getId());
            paymentRepository.save(payment);

            // 4. Monta a resposta populando a URL que o Angular precisa para abrir a tela
            PaymentResponse response = paymentMapper.toResponse(payment);
            response.setCheckoutUrl(preference.getSandboxInitPoint()); // Use getInitPoint() em produção
            return response;

        } catch (MPApiException e) {
            System.err.println("MercadoPago API Error: " + e.getApiResponse().getContent());
            throw new BusinessException("Falha na API do Mercado Pago: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            throw new BusinessException("Falha ao se comunicar com o gateway de pagamento: " + e.getMessage());
        }
    }

    @Override
    @Transactional // ADICIONADO: Garante a atomicidade das alterações no banco
    public void processarNotificacaoWebhook(Long dataId, String type, Map<String, Object> payload) {
        try {
            Long mpPaymentId = null;

            if (dataId != null && "payment".equals(type)) {
                mpPaymentId = dataId;
            } else if (payload != null && payload.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                if (data != null && data.containsKey("id")) {
                    mpPaymentId = Long.parseLong(data.get("id").toString());
                }
            }

            if (mpPaymentId != null) {
                PaymentClient client = new PaymentClient();
                com.mercadopago.resources.payment.Payment mpPayment = client.get(mpPaymentId);

                String status = mpPayment.getStatus();
                String externalReference = mpPayment.getExternalReference();

                if (externalReference != null) {
                    Long localPaymentId = Long.parseLong(externalReference);
                    Payment localPayment = paymentRepository.findById(localPaymentId)
                            .orElseThrow(() -> new EntityNotFoundException("Pagamento local não encontrado"));

                    Order order = localPayment.getOrder(); // Recupera o pedido vinculado

                    // Mapeamento de Status de Pagamento e atualização automática do Pedido
                    if ("approved".equals(status)) {
                        localPayment.setStatus(PaymentStatus.COMPLETED);
                        order.setStatus(OrderStatus.PAID); // <-- Pedido atualizado para PAGO!
                        System.out.println("Pedido #" + order.getId() + " atualizado para PAID.");

                    } else if ("rejected".equals(status) || "cancelled".equals(status)) {
                        localPayment.setStatus(PaymentStatus.FAILED);

                        // NOTA DE NEGÓCIO: Geralmente mantemos o pedido como PENDING para o cliente
                        // tentar pagar de novo. Se preferir cancelar direto o pedido e devolver o
                        // estoque,
                        // descomente as linhas abaixo:
                        // order.setStatus(OrderStatus.CANCELED);
                        // para devolver o estoque aqui, precisaríamos expor a lógica ou usar o
                        // orderRepository
                    }

                    paymentRepository.save(localPayment); // O Hibernate cuida de salvar a order alterada por
                                                          // cascateamento/dirty checking
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar webhook: " + e.getMessage());
        }
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