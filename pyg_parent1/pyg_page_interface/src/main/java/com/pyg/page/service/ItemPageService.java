package com.pyg.page.service;
//商品详情页接口
public interface ItemPageService {
    //生成商品详情页
    public boolean genItemHtml(Long goodsId);
    public boolean deleteItemHtml(Long[] goodsIds);
}
