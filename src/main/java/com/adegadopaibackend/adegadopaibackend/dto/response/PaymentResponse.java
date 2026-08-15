package com.adegadopaibackend.adegadopaibackend.dto.response;

import com.adegadopaibackend.adegadopaibackend.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private Long id;

    private BigDecimal amount;

    private PaymentStatus status;

    private String transactionId;

    private Long orderId;

    private Long paymentMethodId;

    private String checkoutUrl;

    private LocalDateTime createdAt;
}
