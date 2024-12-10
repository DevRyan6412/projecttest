package com.shop.dto;

import lombok.Getter;

@Getter
public class CouponApplyResponse {
    private final double originalPrice;  // 원래 가격
    private final double discount;       // 할인 금액
    private final double finalPrice;     // 최종 가격

    public CouponApplyResponse(double originalPrice, double discount, double finalPrice) {
        this.originalPrice = originalPrice;
        this.discount = discount;
        this.finalPrice = finalPrice;
    }
}
