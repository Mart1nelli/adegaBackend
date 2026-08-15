package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreatePaymentRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;
import com.adegadopaibackend.adegadopaibackend.entity.enums.PaymentStatus;
import com.adegadopaibackend.adegadopaibackend.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    void createPayment_ShouldReturnCreated() throws Exception {
        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .orderId(1L)
                .paymentMethodId(1L)
                .build();

        PaymentResponse res = PaymentResponse.builder()
                .id(100L)
                .orderId(1L)
                .paymentMethodId(1L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .checkoutUrl("https://sandbox.mercadopago.com.br/checkout/v1/redirect?pref_id=123")
                .build();

        when(paymentService.create(any())).thenReturn(res);

        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.checkoutUrl").value("https://sandbox.mercadopago.com.br/checkout/v1/redirect?pref_id=123"));
    }

    @Test
    void findMyPayments_ShouldReturnOk() throws Exception {
        PaymentResponse res = PaymentResponse.builder()
                .id(100L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentService.findMyPayments()).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/payments/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void findById_ShouldReturnOk() throws Exception {
        PaymentResponse res = PaymentResponse.builder()
                .id(100L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentService.findById(100L)).thenReturn(res);

        mockMvc.perform(get("/api/v1/payments/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void findByOrderId_ShouldReturnOk() throws Exception {
        PaymentResponse res = PaymentResponse.builder()
                .id(100L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentService.findByOrderId(10L)).thenReturn(List.of(res));

        mockMvc.perform(get("/api/v1/payments/order/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100));
    }

    @Test
    void syncPaymentStatus_ShouldReturnOk() throws Exception {
        PaymentResponse res = PaymentResponse.builder()
                .id(100L)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentService.syncPaymentStatus(100L)).thenReturn(res);

        mockMvc.perform(post("/api/v1/payments/100/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void webhook_ShouldBeAccessible() throws Exception {
        mockMvc.perform(post("/api/v1/payments/webhook")
                        .param("type", "payment")
                        .param("data.id", "999999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        verify(paymentService).processarNotificacaoWebhook(any(), any());
    }
}
