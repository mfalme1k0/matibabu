package com.matibabu.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    private final CorsConfig config = new CorsConfig();

    @Test
    void parsesCommaSeparatedOriginsAndTrimsWhitespace() {
        UrlBasedCorsConfigurationSource source =
                config.corsConfigurationSource("http://localhost:3000, https://emr.example.org ,");

        CorsConfiguration cors = source.getCorsConfigurations().get("/api/**");

        assertEquals(List.of("http://localhost:3000", "https://emr.example.org"), cors.getAllowedOrigins());
    }

    @Test
    void coversApiAndSessionEndpointsAndAllowsCredentials() {
        UrlBasedCorsConfigurationSource source = config.corsConfigurationSource("http://localhost:3000");

        Map<String, CorsConfiguration> mappings = source.getCorsConfigurations();

        assertTrue(mappings.keySet().containsAll(List.of("/api/**", "/auth/**", "/csrf", "/login", "/logout")));
        CorsConfiguration cors = mappings.get("/api/**");
        assertEquals(Boolean.TRUE, cors.getAllowCredentials());
        assertTrue(cors.getAllowedMethods().contains("PATCH"));
    }

    @Test
    void rejectsWildcardOriginBecauseCredentialsAreAllowed() {
        assertThrows(IllegalStateException.class, () -> config.corsConfigurationSource("*"));
    }

    @Test
    void rejectsEmptyOriginList() {
        assertThrows(IllegalStateException.class, () -> config.corsConfigurationSource(" , "));
    }
}
