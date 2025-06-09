package com.popoworld.backend.global.token;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;

@Document(collection = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private String id;

    private String userEmail;

    private String token;

    //TTL 필드
    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;
}