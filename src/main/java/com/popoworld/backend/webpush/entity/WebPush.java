package com.popoworld.backend.webpush.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Setter
@Getter
public class WebPush {
    @Id
    @GeneratedValue
    private Long id;

    private UUID userId;  // 로그인 유저 ID
    private String endpoint;
    private String p256dh;
    private String auth;
}
