package com.example.template.quarkus.config;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;

import jakarta.ws.rs.core.Application;

/**
 * Konfigurasi SmallRye OpenAPI (Quarkus).
 * Menggantikan SpringDoc OpenApiConfig.
 *
 * Swagger UI tersedia di: /q/swagger-ui
 */
@OpenAPIDefinition(
        info = @Info(
                title = "REST Template API",
                version = "1.0.0",
                description = "Template REST API dengan Quarkus - JWT Auth, CRUD, Feign, Caching, Tracing",
                contact = @Contact(name = "Singgih Praditya", email = "dev@singgih.co.id")
        )
)
@SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig extends Application {
}
