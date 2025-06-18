package com.popoworld.backend.popoPet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@AllArgsConstructor
@Service
@NoArgsConstructor
@Table(name="popo_pet")
public class PopoPet {

    @Id
    @GeneratedValue
    private UUID popoId;

    private UUID userId; //아이

    private Integer level =1;

    private Integer experience =0; //현재 경험치

    private Integer totalExperience =0; //총 누적 경험치

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    //레벨업로직
    public void addExperience(int exp) {
        this.experience += exp;           // 97 + 10 = 107
        this.totalExperience += exp;      // 총 누적도 증가

        // 레벨업 체크 (100 경험치마다 레벨업)
        while (this.experience >= 100) {  // 107 >= 100 ? true
            this.experience -= 100;       // 107 - 100 = 7
            this.level++;                 // 레벨 1 증가
        }
        // 최종: 레벨업 + 현재경험치 7
    }
    // 새 포포 생성용 정적 메서드
    public static PopoPet createNewPopo(UUID userId) {
        PopoPet popo = new PopoPet();
        popo.userId = userId;
        popo.level = 1;
        popo.experience = 0;
        popo.totalExperience = 0;
        return popo;
    }


}
