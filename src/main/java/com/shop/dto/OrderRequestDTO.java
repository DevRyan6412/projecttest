package com.shop.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {

    // Getters and Setters
    private int purchaseAmount; // 구매 금액
    private int mileageUsed;    // 사용한 마일리지

}
