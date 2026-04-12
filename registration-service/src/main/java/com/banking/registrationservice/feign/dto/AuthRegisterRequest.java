package com.banking.registrationservice.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterRequest {
    private String username;
    private String email;
    private String password;
    private String role;
}
