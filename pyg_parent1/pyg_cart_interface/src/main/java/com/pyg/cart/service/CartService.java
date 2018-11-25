package com.pyg.cart.service;

import com.pyg.pojogroup.Cart;

import java.util.List;

//购物车服务接口
public interface CartService {
    //添加商品到购物车
    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);
    //从redis查询数据库
    public List<Cart> findCartListFromRedis(String userName);
    //将购物车保存到redis
    public void saveCartListToRedis(String userName,List<Cart> cartList);
    //合并购物车
    public List<Cart> mergeCartList(List<Cart> cartList_redis,List<Cart> cartList_cookie);
}
