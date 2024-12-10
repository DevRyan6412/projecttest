package com.shop.service;

import com.shop.dto.CartItemDto;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.CartItemRepository;
import com.shop.repository.CartRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.controller.CartSseController;  // CartSseController import 추가
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

public class CartServiceTests {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CartSseController cartSseController;  // CartSseController mock 추가

    @InjectMocks
    private CartService cartService;

    private Member member;
    private Item item;
    private Cart cart;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        member = new Member();
        member.setId(1L);
        member.setEmail("test@example.com");
        item = new Item();
        item.setId(1L);
        cart = new Cart();
        cart.setId(1L);
        cart.setMember(member);

        // Mock the memberRepository to return the mock member
        when(memberRepository.findByEmail("test@example.com")).thenReturn(member);
        // Mock the CartSseController to prevent NPE
        doNothing().when(cartSseController).sendCartCountUpdate(anyInt());
    }

    @Test
    void addCart_ShouldAddItemToCart_WhenItemIsNotInCart() {
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setItemId(1L);
        cartItemDto.setCount(1);

        when(cartRepository.findByMemberId(member.getId())).thenReturn(cart);
        when(itemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).thenReturn(null);

        Long cartId = cartService.addCart(cartItemDto, member.getEmail());

        assertEquals(cart.getId(), cartId);
        verify(cartItemRepository, times(1)).save(any(CartItem.class)); // verify save method called once
    }

    @Test
    void addCart_ShouldUpdateItemCount_WhenItemIsAlreadyInCart() {
        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(1);
        cartItem.setCart(cart);

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setItemId(1L);
        cartItemDto.setCount(3);

        when(cartRepository.findByMemberId(member.getId())).thenReturn(cart);
        when(itemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId())).thenReturn(cartItem);

        Long cartId = cartService.addCart(cartItemDto, member.getEmail());

        assertEquals(cart.getId(), cartId);
        assertEquals(4, cartItem.getCount()); // 수량이 4로 업데이트되어야 합니다.
        verify(cartItemRepository, times(1)).save(cartItem); // save 메서드가 1번 호출되었는지 확인
    }

}
