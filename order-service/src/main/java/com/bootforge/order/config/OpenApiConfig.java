package com.bootforge.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${app.api-gateway-url:http://localhost:9100}")
    private String apiGatewayUrl;

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .servers(java.util.List.of(
                        new Server()
                                .url(apiGatewayUrl)
                                .description("API Gateway")
                ))
                .info(new Info()
                        .title("Order Service API")
                        .description("REST APIs for managing inventories")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BootForge")));
    }
}
