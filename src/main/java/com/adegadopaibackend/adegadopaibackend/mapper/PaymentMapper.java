package com.adegadopaibackend.adegadopaibackend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.adegadopaibackend.adegadopaibackend.dto.response.PaymentResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "checkoutUrl", ignore = true)
    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "paymentMethodId", source = "method.id")
    PaymentResponse toResponse(Payment payment);

    List<PaymentResponse> toResponseList(List<Payment> payments);
}
