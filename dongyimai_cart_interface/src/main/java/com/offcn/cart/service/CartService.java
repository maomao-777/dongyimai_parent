package com.offcn.cart.service;

import com.wzp.entity.Cart;

import java.util.List;

/**
 * 购物车服务接口
 *
 * @author Administrator
 */
public interface CartService {

    //添加购物车
    public List<Cart> addGoodsToCartList(List<Cart> srccartList, Long itemId, Integer num);

    //根据商家名称在缓存中发现购物车
    public List<Cart> findCartListFromRedis(String username);

    //根据商家名称保存购物车列表
    public void saveCartListRedis(List<Cart> cartList, String username);

    //合并购物车
    public List<Cart> margeCartList(List<Cart>cartList_cookie,List<Cart>cartList_redis);

}
