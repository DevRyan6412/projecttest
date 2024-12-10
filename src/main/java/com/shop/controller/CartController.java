//package com.shop.controller;
//
//
//import com.shop.dto.CartDetailDto;
//import com.shop.dto.CartItemDto;
//import com.shop.dto.CartOrderDto;
//import com.shop.service.CartService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//import java.security.Principal;
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class CartController {
//    private final CartService cartService;
//
//    @PostMapping(value = "/cart")
//    public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto, BindingResult bindingResult, Principal principal) {
//
//        if(bindingResult.hasErrors()){
//            StringBuilder sb = new StringBuilder();
//            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
//            for (FieldError fieldError : fieldErrors){
//                sb.append(fieldError.getDefaultMessage());
//            }
//            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
//        }
//
//        String email = principal.getName();
//        Long cartItemId;
//
//        try {
//            cartItemId = cartService.addCart(cartItemDto, email);
//        } catch(Exception e) {
//            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
//    }
//
//    @GetMapping(value = "/cart")
//    public String cartHist(Principal principal, Model model){
//        List<CartDetailDto> cartDetailList = cartService.getCartList(principal.getName());
//        model.addAttribute("cartItems", cartDetailList);
//        return "cart/cartList";
//    }
//
//    @PatchMapping(value = "/cartItem/{cartItemId}")
//    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId, int count, Principal principal){
//        if(count <= 0) {
//            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
//        } else if(!cartService.validateCartItem(cartItemId, principal.getName())){
//            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
//        }
//
//        cartService.updateCartItemCount(cartItemId, count);
//        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
//    }
//
//    @DeleteMapping(value = "/cartItem/{cartItemId}")
//    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId, Principal principal){
//
//    if(!cartService.validateCartItem(cartItemId, principal.getName())){
//        return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
//        }
//    cartService.deleteCartItem(cartItemId);
//    return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
//    }
//
//    @PostMapping(value = "/cart/orders")
//    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto, Principal principal){
//        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();
//
//        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0) {
//            return new ResponseEntity<String>("주문할 상품을 선택 해주세요", HttpStatus.FORBIDDEN);
//        }
//        for(CartOrderDto cartOrder : cartOrderDtoList) {
//            if(!cartService.validateCartItem(cartOrder.getCartItemId(), principal.getName())){
//                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
//            }
//        }
//
//        Long orderId = cartService.orderCartItem(cartOrderDtoList, principal.getName());
//        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
//    }
//}



package com.shop.controller;

import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    private String getEmailFromPrincipal(Principal principal) {
        if (principal == null) return null;

        // 현재 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // OAuth2 로그인인 경우
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2User oauth2User = ((OAuth2AuthenticationToken) authentication).getPrincipal();
            Map<String, Object> attributes = oauth2User.getAttributes();

            // 카카오 계정의 경우
            if (attributes.containsKey("kakao_account")) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                return (String) kakaoAccount.get("email");
            }
        }

        // 일반 로그인의 경우
        return principal.getName();
    }

    @PostMapping(value = "/cart")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid CartItemDto cartItemDto,
                                              BindingResult bindingResult, Principal principal) {

        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors){
                sb.append(fieldError.getDefaultMessage());
            }
            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = getEmailFromPrincipal(principal);
        if (email == null) {
            return new ResponseEntity<String>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        Long cartItemId;
        try {
            cartItemId = cartService.addCart(cartItemDto, email);
        } catch(Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @GetMapping(value = "/cart")
    public String cartHist(Principal principal, Model model){
        String email = getEmailFromPrincipal(principal);
        List<CartDetailDto> cartDetailList = cartService.getCartList(email);
        model.addAttribute("cartItems", cartDetailList);
        return "cart/cartList";
    }

    @PatchMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity updateCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       int count, Principal principal){
        if(count <= 0) {
            return new ResponseEntity<String>("최소 1개 이상 담아주세요", HttpStatus.BAD_REQUEST);
        }

        String email = getEmailFromPrincipal(principal);
        if(!cartService.validateCartItem(cartItemId, email)){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        cartService.updateCartItemCount(cartItemId, count);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @DeleteMapping(value = "/cartItem/{cartItemId}")
    public @ResponseBody ResponseEntity deleteCartItem(@PathVariable("cartItemId") Long cartItemId,
                                                       Principal principal){
        String email = getEmailFromPrincipal(principal);
        if(!cartService.validateCartItem(cartItemId, email)){
            return new ResponseEntity<String>("수정 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        cartService.deleteCartItem(cartItemId);
        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

    @PostMapping(value = "/cart/orders")
    public @ResponseBody ResponseEntity orderCartItem(@RequestBody CartOrderDto cartOrderDto,
                                                      Principal principal){
        List<CartOrderDto> cartOrderDtoList = cartOrderDto.getCartOrderDtoList();

        if(cartOrderDtoList == null || cartOrderDtoList.size() == 0) {
            return new ResponseEntity<String>("주문할 상품을 선택 해주세요", HttpStatus.FORBIDDEN);
        }

        String email = getEmailFromPrincipal(principal);
        for(CartOrderDto cartOrder : cartOrderDtoList) {
            if(!cartService.validateCartItem(cartOrder.getCartItemId(), email)){
                return new ResponseEntity<String>("주문 권한이 없습니다.", HttpStatus.FORBIDDEN);
            }
        }

        Long orderId = cartService.orderCartItem(cartOrderDtoList, email);
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }
}