package com.matibabu.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Metadata for the springdoc-generated OpenAPI document (/v3/api-docs).
 * The curated contract used by Swagger UI by default is served from
 * {@code classpath:/static/openapi.yaml} (see application.properties).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI matibabuOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Matibabu EMR Lite API")
                        .description("""
                                REST API for the Matibabu Offline-First EMR Lite system.

                                The curated OpenAPI document (Patients, Departments, Encounters)
                                is the default definition in Swagger UI. The generated document
                                reflects controller signatures discovered at runtime.
                                """)
                        .version("1.1.0")
                        .contact(new Contact()
                                .name("Matibabu Development Team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}
