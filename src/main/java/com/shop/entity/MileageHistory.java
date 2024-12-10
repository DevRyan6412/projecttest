package com.shop.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mileage_history")
@Getter
@Setter
@NoArgsConstructor
public class MileageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 마일리지 내역 고유 ID

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 회원 정보 (외래 키)

    @Column(nullable = false)
    private int amount; // 마일리지 변동량 (+ 적립, - 사용)

    @Column(nullable = false)
    private String type; // 내역 유형 (적립: earn, 사용: use)

    @Column
    private String description; // 내역 설명

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 내역 생성 시간
}
