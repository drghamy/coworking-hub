package com.coworking.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username is required to login")
    private String username;

    @NotBlank(message = "Password is required to login")
    private String password;
}