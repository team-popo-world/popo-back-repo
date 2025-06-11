package com.popoworld.backend.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
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
    private String role; // "Parent" or "Child"
    private String parentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private User parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<User> children = new ArrayList<>();

    private int point;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public UUID getParentId() {
        return parent != null ? parent.getUserId() : null;
    }
}

