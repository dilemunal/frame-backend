package com.dilem.framebackend.model.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
    @NotBlank(message = "Identity token is required")
    String idToken
) {}
