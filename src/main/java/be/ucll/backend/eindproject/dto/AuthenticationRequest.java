package be.ucll.backend.eindproject.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank String email,
        @NotBlank String password
) {}
