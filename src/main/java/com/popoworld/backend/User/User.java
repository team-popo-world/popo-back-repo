package com.popoworld.backend.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
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

    private int point;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "tutorial_completed")
    private Boolean tutorialCompleted = false; //튜토리얼 완료 여부

    public boolean isTutorialCompleted() {
        return tutorialCompleted != null && tutorialCompleted;
    }

    //포인트 차감 메서드
    public void deductPoints(int amount) {
        if (this.point < amount) {
            throw new IllegalStateException("보유 포인트가 부족합니다. 현재 포인트: " + this.point + ", 필요 포인트: " + amount);
        }
        this.point -= amount;
    }

    //포인트 추가 메서드
    public void addPoints(int amount) {
        this.point += amount;
    }

    //포인트 충분한지 확인
    public boolean hasEnoughPoints(int amount) {
        return this.point >= amount;
    }
}

