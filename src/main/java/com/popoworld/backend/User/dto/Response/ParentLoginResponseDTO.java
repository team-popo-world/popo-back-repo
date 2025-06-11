package com.popoworld.backend.User.dto.Response;

import com.popoworld.backend.User.User;
import com.popoworld.backend.User.dto.ChildInfoDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParentLoginResponseDTO extends LoginResponseDTO {
    private String parentCode;
    private List<ChildInfoDTO> children;

    public ParentLoginResponseDTO(String accessToken, String refreshToken, String role, String name, String parentCode, List<ChildInfoDTO> children) {
        super(accessToken, refreshToken, role, name);
        this.parentCode = parentCode;
        this.children = children;
    }
}
