package com.popoworld.backend.User.dto.Request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SignupRequestDTO {
    private String email;
    @Size(min = 8, message = "비밀번호 최소 8자 이상 작성해 주세요.")
    private String password;
    private String name;
    private String sex;
    private int age;
    private String role;
    private String parentCode;
}
