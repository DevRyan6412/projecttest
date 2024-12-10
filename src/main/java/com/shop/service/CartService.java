package com.shop.service;

import com.shop.controller.CartSseController;
import com.shop.dto.CartDetailDto;
import com.shop.dto.CartItemDto;
import com.shop.dto.CartOrderDto;
import com.shop.dto.OrderDto;
import com.shop.entity.*;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;
    private final CartSseController cartSseController;

    // 장바구니에 상품 추가
    public Long addCart(CartItemDto cartItemDto, String email) {
        Member member = memberRepository.findByEmail(email);
        Cart cart = getOrCreateCart(member);

        // 상품 찾기
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(() -> new EntityNotFoundException("Item not found"));

        // 이미 장바구니에 해당 상품이 있다면 수량만 추가
        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());
        if (savedCartItem != null) {
            savedCartItem.addCount(cartItemDto.getCount());
            cartItemRepository.save(savedCartItem);  // 수량 업데이트 후 저장
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());

            // 마일리지 적용 후 최종 가격 계산
            int availablePrice = item.getPrice() * cartItemDto.getCount();
            cartItem.setFinalPrice(Math.max(availablePrice, 1));  // 최소 1원 이상으로 설정
            cartItem.setMileageApplied(0);  // 초기 마일리지 사용량 설정

            cartItemRepository.save(cartItem);  // 새로운 CartItem 저장
        }

        // 상품 갯수 업데이트 전송
        sendCartCountUpdate(email);

        return cart.getId();
    }



    // 장바구니 조회
    @Transactional(readOnly = true)
    public List<CartDetailDto> getCartList(String email) {
        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) return new ArrayList<>();

        return cartItemRepository.findCartDetailDtoList(cart.getId());
    }

    // 장바구니 아이템 삭제
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

        cartItemRepository.delete(cartItem);

        // 상품 갯수 업데이트 전송
        String email = cartItem.getCart().getMember().getEmail();
        sendCartCountUpdate(email);
    }

    // 장바구니 아이템 수량 업데이트
    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

        cartItem.updateCount(count);

        // 상품 갯수 업데이트 전송
        String email = cartItem.getCart().getMember().getEmail();
        sendCartCountUpdate(email);
    }

    // 장바구니에서 주문 진행
    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {
        List<OrderDto> orderDtoList = new ArrayList<>();

        // 주문 DTO 생성
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        // 주문 처리
        Long orderId = orderService.orders(orderDtoList, email);

        // 장바구니에서 아이템 삭제
        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                    .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));
            cartItemRepository.delete(cartItem);
        }

        // 상품 갯수 업데이트 전송
        sendCartCountUpdate(email);

        return orderId;
    }

    // 마일리지 적용 후 장바구니 금액 계산
    public void applyMileageToCart(Long memberId, int mileageToUse) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        // 해당 사용자의 장바구니 찾기
        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) throw new EntityNotFoundException("Cart not found");

        // 장바구니 아이템에 대해 마일리지 적용
        for (CartItem cartItem : cart.getCartItems()) {
            int availablePrice = cartItem.getItem().getPrice() * cartItem.getCount();
            int finalPrice = Math.max(availablePrice - mileageToUse, 1);

            cartItem.setFinalPrice(finalPrice); // 최종 결제 금액 업데이트
            cartItem.setMileageApplied(mileageToUse); // 사용된 마일리지 저장
        }

        // 마일리지 차감
        member.setAvailableMileage(member.getAvailableMileage() - mileageToUse); // Member의 마일리지 차감
    }

    // 이메일을 기반으로 장바구니 상품 갯수 계산
    @Transactional(readOnly = true)
    public int getCartItemCount(String email) {
        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());
        return cart == null ? 0 : cartItemRepository.countByCartId(cart.getId());
    }

    // 상품 갯수 업데이트 후 SSE 이벤트 전송
    private void sendCartCountUpdate(String email) {
        int cartItemCount = getCartItemCount(email); // 상품 갯수 계산
        cartSseController.sendCartCountUpdate(cartItemCount); // SSE 전송
    }

    // Cart가 없으면 새로 생성하는 로직
    private Cart getOrCreateCart(Member member) {
        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            cart = Cart.createCart(member);  // Cart 생성 메소드 호출
            cartRepository.save(cart);
        }
        return cart;
    }

    // 장바구니 아이템 권한 확인
    @Transactional(readOnly = true)
    public boolean validateCartItem(Long cartItemId, String email) {
        // 해당 cartItem을 찾기
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));

        // 해당 cartItem이 속한 cart와 cart의 owner(member)를 확인
        Cart cart = cartItem.getCart();
        Member member = cart.getMember();

        // 해당 이메일과 회원의 이메일이 일치하는지 확인
        return member.getEmail().equals(email);
    }
}
