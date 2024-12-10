package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponApplyRequest {
    private String couponCode;   // 쿠폰 코드
    private double originalPrice; // 원래 가격
}
