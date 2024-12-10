package com.shop.service;

import com.shop.dto.CouponDTO;
import com.shop.entity.Coupon;
import com.shop.repository.CouponRepository;
import com.shop.util.CouponUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    // 쿠폰 생성
    public CouponDTO createCoupon(String description, Double discountRate, Double discountAmount, String discountType) {
        if ("percent".equals(discountType)) {
            if (discountRate == null || discountRate < 0 || discountRate > 100) {
                throw new IllegalArgumentException("퍼센트 할인율은 0 ~ 100 사이여야 합니다.");
            }
            discountAmount = 0.0;
        } else if ("amount".equals(discountType)) {
            if (discountAmount == null || discountAmount < 0) {
                throw new IllegalArgumentException("금액 할인은 0 이상이어야 합니다.");
            }
            discountRate = 0.0;
        } else {
            throw new IllegalArgumentException("할인 타입은 'percent' 또는 'amount'만 가능합니다.");
        }

        // 쿠폰 생성
        Coupon coupon = new Coupon(description, discountRate / 100, discountAmount, discountType);
        coupon.setCouponCode(CouponUtils.generateRandomCouponCode());
        couponRepository.save(coupon);

        return new CouponDTO(
                coupon.getCouponCode(),
                coupon.getDescription(),
                coupon.getDiscountType(),
                coupon.getDiscountRate(),
                coupon.getDiscountAmount()
        );
    }

    // 쿠폰 조회
    public Coupon findCouponByCode(String couponCode) {
        return couponRepository.findById(couponCode)
                .orElseThrow(() -> new EntityNotFoundException("쿠폰이 존재하지 않습니다: " + couponCode));
    }

    // 쿠폰 할인 계산
    public double applyCoupon(String couponCode, double originalPrice) {
        Coupon coupon = findCouponByCode(couponCode);
        return coupon.calculateDiscount(originalPrice);
    }

    // 쿠폰 삭제
    public void deleteCoupon(String couponCode) {
        Coupon coupon = findCouponByCode(couponCode);
        couponRepository.delete(coupon);
    }

    // 모든 쿠폰 조회
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(coupon -> new CouponDTO(
                        coupon.getCouponCode(),
                        coupon.getDescription(),
                        coupon.getDiscountType(),
                        coupon.getDiscountRate(),
                        coupon.getDiscountAmount()))
                .collect(Collectors.toList());
    }
}
