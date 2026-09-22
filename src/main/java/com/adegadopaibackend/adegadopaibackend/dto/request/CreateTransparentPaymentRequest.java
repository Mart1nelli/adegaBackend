package com.adegadopaibackend.adegadopaibackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateTransparentPaymentRequest {

    @NotNull
    @Positive
    private Long orderId;

    @NotNull
    @Positive
    private Long paymentMethodId;

    @NotBlank
    private String mercadoPagoPaymentMethodId;

    private String token;

    @Positive
    private Integer installments;

    private String issuerId;

    private String payerEmail;
}
