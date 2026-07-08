package com.adegadopaibackend.adegadopaibackend.dto.response;

import com.adegadopaibackend.adegadopaibackend.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {

    private Long id;

    private BigDecimal amount;

    private PaymentStatus status;

    private String transactionId;

    private String checkoutUrl;
}
