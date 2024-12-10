package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@Setter
@ToString
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "cart_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;  // 장바구니에 담긴 상품들

    // 장바구니 총 금액 계산
    public double getTotalAmount() {
        return cartItems.stream()
                .mapToDouble(cartItem -> cartItem.getItem().getPrice() * cartItem.getCount())
                .sum();
    }

    // 마일리지 적용
    public void applyMileage(double availableMileage) {
        double totalAmount = getTotalAmount();
        double mileageToUse = Math.min(availableMileage, totalAmount);

        for (CartItem cartItem : cartItems) {
            double itemTotalPrice = cartItem.getItem().getPrice() * cartItem.getCount();
            double itemFinalPrice = itemTotalPrice - (itemTotalPrice * (mileageToUse / totalAmount));
            cartItem.setFinalPrice(itemFinalPrice);  // 각 상품에 최종 가격 설정
        }
    }

    // 최종 결제 금액 계산
    public double getFinalAmount() {
        return cartItems.stream()
                .mapToDouble(CartItem::getFinalPrice)
                .sum();
    }

    // Cart 생성 메소드
    public static Cart createCart(Member member) {
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }
}
