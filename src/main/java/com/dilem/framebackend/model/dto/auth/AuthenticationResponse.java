package com.dilem.framebackend.model.dto.auth;

import com.dilem.framebackend.model.dto.UserDto;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthenticationResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("refresh_token") String refreshToken,
    @JsonProperty("expires_in") long expiresIn,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("user") UserDto user
) {
    public static AuthenticationResponse of(String accessToken, String refreshToken, long expiresIn, UserDto user) {
        return new AuthenticationResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
    }
}
