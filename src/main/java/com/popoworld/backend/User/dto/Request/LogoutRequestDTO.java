package com.popoworld.backend.User.dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequestDTO {
    private String refreshToken;
}
