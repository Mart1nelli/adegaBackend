package com.adegadopaibackend.adegadopaibackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:adegadopaibackend;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.flyway.enabled=false",
		"security.jwt.secret-key=12345678901234567890123456789012",
		"mercadopago.access-token=TEST-TOKEN",
		"mercadopago.webhook-url=http://localhost:8080/api/v1/payments/webhook",
		"mercadopago.frontend-success-url=http://localhost:4200/checkout/sucesso",
		"mercadopago.frontend-failure-url=http://localhost:4200/checkout/falha",
		"mercadopago.frontend-pending-url=http://localhost:4200/checkout/pendente",
		"mercadopago.use-sandbox-checkout-url=true"
})
class AdegadopaibackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
