package com.popoworld.backend.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue
    private UUID userId;

    @Column(unique = true)
    private String email;
    private String password;
    private String sex;
    private int age;
    private String name;
    private String role; // Parent or Child
    private String parentCode;
    private UUID parentId;
    private int point;
    @CreationTimestamp
    private LocalDateTime createdAt;
}
