package com.adegadopaibackend.adegadopaibackend.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoConfiguration {

    private final MercadoPagoProperties properties;

    @PostConstruct
    public void init() {
        if (properties.accessToken() != null && !properties.accessToken().isBlank()) {
            MercadoPagoConfig.setAccessToken(properties.accessToken().trim());
            log.info("Mercado Pago SDK initialized successfully.");
        } else {
            log.warn("Mercado Pago access token is not configured. Payment preference creation will require MERCADOPAGO_ACCESS_TOKEN.");
        }
    }
}
