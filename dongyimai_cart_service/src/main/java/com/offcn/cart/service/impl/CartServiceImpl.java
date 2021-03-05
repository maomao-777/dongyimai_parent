package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.wzp.entity.Cart;
import com.wzp.mapper.TbItemMapper;
import com.wzp.pojo.TbItem;
import com.wzp.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> srccartList, Long itemId, Integer num) {
        //1.根据ItemId查询sku商品信息
        TbItem tbItem = itemMapper.selectByPrimaryKey(itemId);
        //1.1 判断商品是否存在,防止买到不存在的商品
        if (tbItem == null) {
            throw new RuntimeException("该商品不存在！");
        }
        //1.2判断sku信息商品是否审核通过
        if (!tbItem.getStatus().equals("1")) {
            throw new RuntimeException("该商品未审核！");
        }

        /* -----------组装购物车对象-----------*/

        //2.获取商家ID
        String sellerId = tbItem.getSellerId();
        String sellerName = tbItem.getSeller();


        //3.根据商家ID盘点购物车列表中是否存在该商家的购物车
        Cart cart = this.findCartBySellerId(srccartList, sellerId);

        if (cart == null) {
            //4.如果购物车列表中不存在该商家的购物车
            //4.1新建购物车
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(sellerName);
            List<TbOrderItem> tbOrderItemList = new ArrayList<TbOrderItem>();//购物车详情
            //添加购物车详情
            TbOrderItem tbOrderItem = this.setTbOrderItem(tbItem, num);
            tbOrderItemList.add(tbOrderItem);
            cart.setOrderItemList(tbOrderItemList);
            srccartList.add(cart);

            //4.2将新建的购物车对象添加到购物车列表
        } else {

            //5.如果购物车列表中存在该商家的购物车
            //查询购物车列表中是否存在该商品明细
            TbOrderItem tbOrderItem = this.searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            //5.1如果没有，新增购物车明细
            if (tbOrderItem == null) {
                tbOrderItem = this.setTbOrderItem(tbItem, num);
                cart.getOrderItemList().add(tbOrderItem);
            } else {
                //5.2如果有在元购物车明细上添加数量
                tbOrderItem.setNum(tbOrderItem.getNum() + num);
                //，更改小计金额
                tbOrderItem.setTotalFee(tbOrderItem.getPrice().multiply(new BigDecimal(tbOrderItem.getNum())));
                //订单详情存在的时候顾客可能不喜欢，删除购物详情，删除商家购物车
                if (tbOrderItem.getNum() == 0) {
                    cart.getOrderItemList().remove(tbOrderItem);
                }
                if (cart.getOrderItemList().size() == 0) {
                    srccartList.remove(cart);


                }
            }
        }


        return srccartList;
    }

    //根据商家Id去查询购物车列表中 是否有 该商家的购物车
    private Cart findCartBySellerId(List<Cart> srcCartList, String SellerId) {
        for (Cart cart : srcCartList) {
            if (cart.getSellerId().equals(SellerId)) {
                return cart;
            }
        }
        return null;
    }

    //设置购物车详情
    private TbOrderItem setTbOrderItem(TbItem tbItem, Integer num) {

        //避免数量不合法
        if (num < 1) {
            throw new RuntimeException("购买数量不合法！");
        }
        if (num < 1) {
            throw new RuntimeException("购买数量非法");
        }
        TbOrderItem tbOrderItem = new TbOrderItem();
        tbOrderItem.setItemId(tbItem.getId());                                              //SKU的ID
        tbOrderItem.setGoodsId(tbItem.getGoodsId());                                        //SPU的ID
        tbOrderItem.setSellerId(tbItem.getSellerId());                                      //商家的ID
        tbOrderItem.setNum(num);                                                            //购买数量
        tbOrderItem.setPrice(tbItem.getPrice());                                            //商品单价
        tbOrderItem.setTotalFee(tbOrderItem.getPrice().multiply(new BigDecimal(num)));      //总金额
        tbOrderItem.setTitle(tbItem.getTitle());                                            //商品标题
        tbOrderItem.setPicPath(tbItem.getImage());                                          //商品图片
        return tbOrderItem;
    }

    //查询订单详情是否在订单列表内
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> tbOrderItemList, Long itemId) {
        for (TbOrderItem tbOrderItem : tbOrderItemList) {
            if (tbOrderItem.getItemId().longValue() == itemId.longValue()) {
                return tbOrderItem;
            }
        }
        return null;
    }


    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从缓存中获取商家购物车列表成功！");
        List<Cart> cartList=(List<Cart>)redisTemplate.boundHashOps("cartList").get(username);
        //防止缓存中读取为空，所以遍历之前加判空操作
        if(CollectionUtils.isEmpty(cartList)){
            cartList=new ArrayList<Cart>();
        }

        return cartList;
    }

    @Override
    public void saveCartListRedis(List<Cart> cartList, String username) {
     redisTemplate.boundHashOps("cartList").put(username,cartList);
        System.out.println("向缓存中添加购物车成功！");
    }
//合并购物车
    @Override
    public List<Cart> margeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis) {
        for (Cart cart : cartList_cookie) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                //将cokkie购物车信息放置到缓存中
              cartList_redis = addGoodsToCartList(cartList_redis, tbOrderItem.getItemId(), tbOrderItem.getNum());
            }
        }
        return cartList_redis;
    }
}
