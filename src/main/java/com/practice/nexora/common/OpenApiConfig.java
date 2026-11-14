package com.practice.nexora.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI nexoraOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Nexora Store API")
                .version("v1")
                .description("Catalog of phones, laptops, tablets, wearables and accessories."))
                .components(new Components().addSecuritySchemes("bearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
