package com.shop.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "cart") //데이터 베이스에 생성되는 table 이름을 지정해주고싶을때 사용함
@Getter
@Setter
@ToString
public class Cart extends BaseEntity{

    @Id
    @Column(name = "cart_id") //테이블에 column의 이름을 지정해주고싶을떄
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne//원투원을 지정하면 테이블상에서 FKEY(외래키)가 됨
    @JoinColumn(name="member_id")// 매핑할 외래키의 name으로 이름을 지정
    private Member member;

    public static Cart createCart(Member member) {
        Cart cart = new Cart();
        cart.setMember(member);
        return cart;
    }
}
