package com.adegadopaibackend.adegadopaibackend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@OpenAPIDefinition(info = @Info(title = "API Adega do Pai", version = "1.0", description = "Sistema de Gerenciamento de Vendas e Estoque"))
@SpringBootApplication
@ConfigurationPropertiesScan
public class AdegadopaibackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdegadopaibackendApplication.class, args);
	}

}
