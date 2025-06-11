package com.popoworld.backend.User.dto;

import com.popoworld.backend.User.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ChildInfoDTO {
    private UUID userId;
    private String email;
    private String sex;
    private int age;
    private String name;
    private int point;
    private LocalDateTime createdAt;

    @Builder
    public ChildInfoDTO(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.sex = user.getSex();
        this.age = user.getAge();
        this.name = user.getName();
        this.point = user.getPoint();
        this.createdAt = user.getCreatedAt();
    }
}
