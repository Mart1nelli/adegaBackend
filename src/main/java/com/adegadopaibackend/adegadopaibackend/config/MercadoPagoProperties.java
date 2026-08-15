package com.adegadopaibackend.adegadopaibackend.config;

import com.mercadopago.resources.preference.Preference;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mercadopago")
public record MercadoPagoProperties(
        String accessToken,
        String webhookUrl,
        String frontendSuccessUrl,
        String frontendFailureUrl,
        String frontendPendingUrl,
        boolean useSandboxCheckoutUrl
) {

    public String resolveCheckoutUrl(Preference preference) {
        if (preference == null) {
            throw new IllegalStateException("Mercado Pago preference is null");
        }

        String sandboxUrl = preference.getSandboxInitPoint();
        String productionUrl = preference.getInitPoint();

        if (useSandboxCheckoutUrl && hasText(sandboxUrl)) {
            return sandboxUrl;
        }

        if (hasText(productionUrl)) {
            return productionUrl;
        }

        if (hasText(sandboxUrl)) {
            return sandboxUrl;
        }

        throw new IllegalStateException("Mercado Pago did not return a checkout URL");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
