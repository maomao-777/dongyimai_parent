package com.wzp.group;

import com.wzp.pojo.TbGoods;
import com.wzp.pojo.TbGoodsDesc;
import com.wzp.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

public class Goods implements Serializable {

    private TbGoods goods; //商品信息 SPU

    private TbGoodsDesc goodsDesc; //商品扩展信息

    private List<TbItem> itemList; //商品SKU列表

    public Goods() {
    }

    public Goods(TbGoods goods, TbGoodsDesc goodsDesc, List<TbItem> itemList) {
        this.goods = goods;
        this.goodsDesc = goodsDesc;
        this.itemList = itemList;
    }

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }
}
