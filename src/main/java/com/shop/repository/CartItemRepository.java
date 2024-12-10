package com.shop.repository;

import com.shop.dto.CartDetailDto;
import com.shop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // 장바구니의 특정 아이템을 찾는 메소드
    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    // 장바구니 상세 정보 조회 (상품명, 가격, 이미지 URL 등)
    @Query("select new com.shop.dto.CartDetailDto(ci.id, i.itemNm, i.price, ci.count, im.imgUrl) " +
            "from CartItem ci, ItemImg im " +
            "join ci.item i " +
            "where ci.cart.id = :cartId " +
            "and im.item.id = ci.item.id " +
            "and im.repimgYn = 'Y' " +
            "order by ci.regTime desc")
    List<CartDetailDto> findCartDetailDtoList(Long cartId);

    // 장바구니에 담긴 아이템 수를 세는 메소드
    @Query("select count(ci) from CartItem ci where ci.cart.id = :cartId")
    int countByCartId(Long cartId);

    // 장바구니에서 총 금액 계산하는 메소드
    @Query("select sum(ci.item.price * ci.count) from CartItem ci where ci.cart.id = :cartId")
    Double getTotalAmountByCartId(Long cartId);
}
