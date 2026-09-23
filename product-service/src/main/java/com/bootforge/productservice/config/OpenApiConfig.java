package com.bootforge.productservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Value("${app.api-gateway-url:http://localhost:9100}")
    private String apiGatewayUrl;

    @Bean
    public OpenAPI productServiceOpenAPI() {

        return new OpenAPI()
                .servers(java.util.List.of(
                        new Server()
                                .url(apiGatewayUrl)
                                .description("API Gateway")
                ))
                .info(new Info()
                        .title("Product Service API")
                        .description("REST APIs for managing products")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("BootForge")));
    }
}
