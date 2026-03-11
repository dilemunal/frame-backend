package com.dilem.framebackend.model.dto;

import com.dilem.framebackend.model.User;

public record UserDto(
    Integer id,
    String email,
    String firstName,
    String lastName
) {
    public static UserDto fromEntity(User user) {
        return new UserDto(user.getId(), user.getEmail(), user.getFirstname(), user.getLastname());
    }
}
