package com.popoworld.backend.User.dto.Response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenResponseDTO {
    private String accessToken;
    private String refreshToken;

    public RefreshTokenResponseDTO(String newAccessToken, String newRefreshToken) {
        this.accessToken = newAccessToken;
        this.refreshToken = newRefreshToken;
    }
}
