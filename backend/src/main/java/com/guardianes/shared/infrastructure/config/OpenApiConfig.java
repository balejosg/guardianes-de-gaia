package com.guardianes.shared.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Value("${app.server.url:http://localhost:8080}")
  private String serverUrl;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(apiInfo())
        .servers(
            List.of(
                new Server().url(serverUrl).description("Development server"),
                new Server()
                    .url("https://api.guardianes-de-gaia.com")
                    .description("Production server")));
  }

  private Info apiInfo() {
    return new Info()
        .title("Guardianes de Gaia API")
        .description(
            """
                        REST API for Guardianes de Gaia - A cooperative card game mobile app that gamifies
                        walking to school for families with children aged 6-12. The system converts daily
                        walking steps into energy for card battles, promoting physical activity and family bonding.

                        ## Key Features:
                        * **Step Tracking**: Monitor daily walking activity and convert steps to game energy
                        * **Energy Management**: Spend energy on card battles, challenges, and shop items
                        * **Guardian Profiles**: Manage child player accounts and family groups
                        * **Cooperative Gameplay**: Family-focused card battles with no PvP elements

                        ## Authentication:
                        This API uses session-based authentication. Authenticate using the `/auth/login` endpoint.

                        ## Rate Limiting:
                        API endpoints have rate limiting to prevent abuse. Rate limit headers are included in responses.
                        """)
        .version("1.0.0")
        .contact(
            new Contact()
                .name("Guardianes de Gaia Team")
                .email("dev@guardianes-de-gaia.com")
                .url("https://github.com/guardianes-de-gaia"))
        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"));
  }
}
