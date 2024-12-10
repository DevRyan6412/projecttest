package com.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {
    private String couponCode;      // 쿠폰 코드
    private String description;     // 쿠폰 설명
    private String discountType;    // 할인 타입 (percent: 퍼센트 할인, amount: 금액 할인)
    private Double discountRate;    // 퍼센트 할인율 (0 ~ 100)
    private Double discountAmount;  // 금액 할인 (null일 수 있음)

    // 할인 정보를 출력하는 추가적인 메서드를 넣을 수도 있음
    public String getDiscountInfo() {
        if ("percent".equals(discountType) && discountRate != null) {
            return discountRate + "% 할인";  // discountRate는 이미 0~100 범위로 입력되므로 그대로 표시
        } else if ("amount".equals(discountType) && discountAmount != null) {
            return discountAmount + " 원 할인";
        } else {
            return "할인 정보 없음";
        }
    }
}
