package com.shop.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mileage_summary")
@Getter
@Setter
@NoArgsConstructor
public class MileageSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 마일리지 요약 고유 ID

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원 정보 (외래 키)

    @Column(nullable = false)
    private int totalMileage = 0; // 총 마일리지

    @Column(nullable = false)
    private int totalEarned = 0; // 누적 적립 마일리지

    @Column(nullable = false)
    private int totalUsed = 0; // 누적 사용 마일리지

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now(); // 마지막 갱신 시간

    // 마일리지 적립
    public void earnMileage(int amount) {
        this.totalMileage += amount;
        this.totalEarned += amount;
        this.lastUpdated = LocalDateTime.now();
    }

    // 마일리지 사용
    public void useMileage(int amount) {
        if (this.totalMileage >= amount) {
            this.totalMileage -= amount;
            this.totalUsed += amount;
            this.lastUpdated = LocalDateTime.now();
        } else {
            throw new IllegalArgumentException("Insufficient mileage");
        }
    }
}
