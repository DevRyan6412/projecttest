package com.shop.controller;

import com.shop.dto.CouponApplyRequest;
import com.shop.dto.CouponApplyResponse;
import com.shop.dto.CouponDTO;
import com.shop.entity.Coupon;
import com.shop.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Controller
public class CouponController {

    @Autowired
    private CouponService couponService;

    // 쿠폰 생성 폼을 보여주는 메소드
    @GetMapping("/coupon/create")
    public String showCouponForm() {
        return "redirect:/coupon/list";
    }

    @PostMapping("/coupon/create")
    public String createCoupon(@RequestParam String description,
                               @RequestParam String discountType,
                               @RequestParam(required = false) Double discountRate,
                               @RequestParam(required = false) Double discountAmount,
                               Model model) {

        try {
            // discountRate 또는 discountAmount 중 하나는 필수적으로 제공되어야 함
            CouponDTO couponDTO = couponService.createCoupon(description, discountRate, discountAmount, discountType);
            model.addAttribute("coupon", couponDTO);
            return "redirect:/coupon/list"; // 쿠폰 생성 후 성공 페이지로 이동
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/coupon/list"; // 에러 메시지를 표시하며 다시 폼으로
        }
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyCoupon(@RequestBody CouponApplyRequest request) {
        try {
            double discount = couponService.applyCoupon(request.getCouponCode(), request.getOriginalPrice());
            CouponApplyResponse response = new CouponApplyResponse(
                    request.getOriginalPrice(),
                    discount,
                    request.getOriginalPrice() - discount
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/coupon/delete/{couponCode}")
    public ResponseEntity<?> deleteCoupon(@PathVariable String couponCode) {
        try {
            couponService.deleteCoupon(couponCode);
            return ResponseEntity.ok("쿠폰 삭제 완료");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("쿠폰 삭제 실패");
        }
    }

    @GetMapping("/coupon/list")
    public String listCoupons(Model model) {
        List<CouponDTO> coupons = couponService.getAllCoupons();
        model.addAttribute("coupons", coupons);
        return "coupon/couponList"; // 쿠폰 리스트 페이지로 이동
    }
}

