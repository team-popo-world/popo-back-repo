package com.popoworld.backend.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="daily_check")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID dailyCheckId;

    private UUID childId;

    private LocalDate attendanceDate;

    @PrePersist
    protected void onCreate(){
        if(attendanceDate==null){
            attendanceDate=LocalDate.now();
        }
    }
}
