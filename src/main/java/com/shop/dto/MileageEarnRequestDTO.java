package com.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MileageEarnRequestDTO {

    private Long memberId;        // 회원 ID

    private int purchaseAmount;   // 구매 금액

    public MileageEarnRequestDTO(int i) {
    }
}
