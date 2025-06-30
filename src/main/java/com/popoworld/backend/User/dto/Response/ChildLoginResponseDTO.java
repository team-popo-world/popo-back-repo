package com.popoworld.backend.User.dto.Response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildLoginResponseDTO extends LoginResponseDTO{
    private int point;
    private boolean tutorialCompleted;

    public ChildLoginResponseDTO(String accessToken, String refreshToken, String role, String name, int point,boolean tutorialCompleted) {
        super(accessToken, refreshToken, role, name);
        this.point = point;
        this.tutorialCompleted = tutorialCompleted;
    }
}
