package com.popoworld.backend.savingAccount.entity;

import com.popoworld.backend.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="saving_account")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class SavingAccount {
    @Id
    @GeneratedValue
    private UUID savingAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="child_id")
    private User child;

    private Integer accountPoint; //현재 채워진 금액

    @NotNull
    private Integer goalAmount; //목표 포인트

    @NotNull
    private Integer rewardPoint; //최종 보상

    @NotNull
    private LocalDate createdDate; //통장 개설 날짜

    @NotNull
    private LocalDate endDate; //목표 날짜

    private Boolean success; //달성 성공 여부

    @NotNull
    private Boolean active; //활성화 여부 (true/false)

    private LocalDateTime completedAt; // 완료 시점 (달성/만료 시간)
}
