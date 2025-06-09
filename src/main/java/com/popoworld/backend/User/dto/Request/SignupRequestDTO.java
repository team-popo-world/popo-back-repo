package com.popoworld.backend.User.dto.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDTO {
    private String email;
    private String password;
    private String name;
    private String sex;
    private int age;
    private String role;
    private String parentCode;
}
