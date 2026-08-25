package be.ucll.backend.eindproject.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ticket.validation")
public record TicketValidationProperties(
        @DefaultValue("4") int maxAttempts,
        @DefaultValue("5000") long retryDelayMs,
        @DefaultValue("5000") int connectTimeoutMs,
        @DefaultValue("5000") int readTimeoutMs
) {}
