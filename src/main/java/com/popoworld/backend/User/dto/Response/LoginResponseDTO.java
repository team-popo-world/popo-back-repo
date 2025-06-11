package com.popoworld.backend.User.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String role;
    private String name;
}
