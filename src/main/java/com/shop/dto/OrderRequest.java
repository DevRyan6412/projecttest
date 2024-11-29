package com.shop.dto;


import com.shop.entity.CartItem;

import java.util.List;

public class OrderRequest {

    private Long userid;

    private List<CartItem> cartItems;

}
