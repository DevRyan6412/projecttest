package com.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor  // 기본 생성자 추가
public class MileageSummaryDTO {
    private int totalMileage;     // 총 마일리지
    private int totalEarned;      // 누적 적립 마일리지
    private int totalUsed;        // 누적 사용 마일리지
    private LocalDateTime lastUpdated; // 마지막 갱신 시간
}
