package com.practice.nexora.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI nexoraOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Nexora Store API")
                .version("v1")
                .description("Catalog of phones, laptops, tablets, wearables and accessories."));
    }
}
