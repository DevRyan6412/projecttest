package com.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDetailDto {
    private Long cartItemId; // 장바구니 상품 아이디
    private String itemNm; // 상품명
    private int price; // 상품금액
    private int count; // 수량
    private String imgUrl; // 상품 이미지 경로
    private int discountPrice; // 할인 금액 (선택 사항)
    private int mileageApplied; // 적용된 마일리지 (선택 사항)
    private int finalPrice; // 최종 결제 금액 (마일리지 및 할인 적용 후)

    // 생성자
    public CartDetailDto(Long cartItemId, String itemNm, int price, int count, String imgUrl) {
        this.cartItemId = cartItemId;
        this.itemNm = itemNm;
        this.price = price;
        this.count = count;
        this.imgUrl = imgUrl;
        this.discountPrice = 0;  // 기본값 설정
        this.mileageApplied = 0;  // 기본값 설정
        this.finalPrice = calculateFinalPrice();  // 마일리지와 할인 적용 후 최종 금액 계산
    }


    // 최종 결제 금액 계산 (할인 금액과 마일리지를 반영)
    public int calculateFinalPrice() {
        int totalPrice = price * count;  // 상품 가격에 수량을 곱한 금액
        totalPrice -= discountPrice;  // 할인 금액 차감
        totalPrice -= mileageApplied;  // 적용된 마일리지 차감
        return Math.max(totalPrice, 1); // 결제 금액은 최소 1원
    }

    // 장바구니 상품의 세부 정보를 반환하는 메서드
    public String getCartDetailSummary() {
        return String.format("상품명: %s, 가격: %d원, 수량: %d, 총액: %d원, 할인 금액: %d원, 마일리지 사용: %d원",
                itemNm, price, count, finalPrice, discountPrice, mileageApplied);
    }
}
