package com.offcn.page.service;
// 商品详细页接口
public interface ItemPageService {
    public boolean genItemHtml(Long goodsId);
    public void deleteItemHtml(Long goodsId);
}
