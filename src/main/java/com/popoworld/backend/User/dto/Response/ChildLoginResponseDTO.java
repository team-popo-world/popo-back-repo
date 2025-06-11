package com.popoworld.backend.User.dto.Response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildLoginResponseDTO extends LoginResponseDTO{
    private int point;

    public ChildLoginResponseDTO(String accessToken, String refreshToken, String role, String name, int point) {
        super(accessToken, refreshToken, role, name);
        this.point = point;
    }
}
