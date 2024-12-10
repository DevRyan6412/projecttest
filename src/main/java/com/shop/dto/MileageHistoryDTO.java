package com.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MileageHistoryDTO {
    private int amount;           // 마일리지 변동량
    private String type;          // 적립/사용 유형
    private String description;   // 내역 설명
    private LocalDateTime createdAt; // 생성 시간
}
