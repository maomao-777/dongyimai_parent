package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.order.service.OrderService;
import com.offcn.utils.CookieUtil;
import com.wzp.entity.Cart;
import com.wzp.entity.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //不管登录不登录都从cookie中获取购物车集合
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (StringUtils.isEmpty(cartListStr)) {
            cartListStr = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListStr, Cart.class);
        System.out.println("--------cookie中的数据" + cartList_cookie + "----------");
        if (username.equals("anonymousUser")) {//没有登录
            System.out.println("--------------现在没有登录----------");
            /*//从cookie中获取购物车集合（提为一处）
            String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if (StringUtils.isEmpty(cartListStr)) {
                cartListStr = "[]";
            }*/
            //将JSON结构字符串转换成集合
            /* System.out.println(cartListStr);*/
            return cartList_cookie;
        } else {//登录后
            //登录获取cook中购物车（提为一处）
            //将cookie购物车添加到redis中
            System.out.println("--------登录了-----------");
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if (!CollectionUtils.isEmpty(cartList_cookie)) {//不为空进行合并
                System.out.println("---------合并购物车---------");
                cartList_redis = cartService.margeCartList(cartList_cookie, cartList_redis);
                cartService.saveCartListRedis(cartList_cookie, username);
                //清空cookie购物车
                System.out.println("清空缓存");
                CookieUtil.deleteCookie(request, response, "cartList");
            }
            return cartList_redis;
        }
    }

    //添加购物车集合
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {
        try {
            //允许跨域请求
            response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");
           //允许带有参数
            response.setHeader("Access-Control-Allow-Credentials", "true");
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //取得原有的购物车集合
            List<Cart> cartList = this.findCartList(request, response);
            //将商品添加到购物车集合
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) {
                //将更新过的购物车从新加入cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 60 * 60 * 24, "UTF-8");
            } else {
                cartService.saveCartListRedis(cartList, username);
            }
            return new Result(true, "添加购物车成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加购物车失败！");
        }
    }


}
