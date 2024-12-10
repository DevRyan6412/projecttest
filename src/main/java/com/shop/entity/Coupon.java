package com.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {

    @Id
    @Column(unique = true)
    private String couponCode; // 쿠폰 코드(UUID)

    private String description; // 쿠폰 설명

    private String discountType;  // 퍼센트 또는 금액 할인 타입 (percent, amount)

    private Double discountRate; // 퍼센트 할인율 (0 ~ 1.0, 예: 0.1 = 10% 할인)

    private Double discountAmount; // 금액 할인 (예: 1000원 할인)

    // discountType을 추가한 생성자
    public Coupon(String description, Double discountRate, double discountAmount, String discountType) {
        this.couponCode = UUID.randomUUID().toString();
        this.description = description;
        this.discountRate = discountRate;
        this.discountAmount = discountAmount;
        this.discountType = discountType; // discountType을 설정
    }

    // 할인 계산 (할인 금액을 올림 처리)
    public double calculateDiscount(double originalPrice) {
        double discount;

        if (discountType.equals("percent")) {
            discount = originalPrice * discountRate; // 퍼센트 할인
        } else if (discountType.equals("amount")) {
            discount = discountAmount; // 금액 할인
        } else {
            discount = 0; // 할인 없음
        }

        // 할인 금액은 상품 가격을 초과할 수 없음
        discount = Math.min(discount, originalPrice);

        // 할인 금액을 올림 처리
        return Math.ceil(discount);
    }

}
