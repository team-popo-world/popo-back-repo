package com.popoworld.backend.User.dto.Response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String role;
    private String name;
    private int point;
}
