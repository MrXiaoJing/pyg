package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
//购物车服务实现类
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("商品不存在");
        }
        if(!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item, num);
            List orderItemList = new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新建的购物车对象添加到购物车列表
            cartList.add(cart);
        }else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem==null){
                //5.1. 如果没有，新增购物车明细
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else{
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //如果数量操作后小于0，则移除
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem); //移除购物车明细
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }


    //根据商家id查询购物车对象
    private Cart searchCartBySellerId(List<Cart> cartList,String sellerId){
        for(Cart cart:cartList){
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
    //创建订单详情
    private TbOrderItem createOrderItem(TbItem item,Integer num){
        if(num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());//spuid
        orderItem.setItemId(item.getId());//skuid
        orderItem.setNum(num);//商家数量
        orderItem.setPicPath(item.getImage()); //图片
        orderItem.setPrice(item.getPrice());//价格
        orderItem.setSellerId(item.getSellerId());//商家id
        orderItem.setTitle(item.getTitle());
        //orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));//小计
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
        return orderItem;
    }
    //根据商品明细id查询
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }
    @Autowired
    private RedisTemplate redisTemplate;
    //从redis查询数据库
    @Override
    public List<Cart> findCartListFromRedis(String userName) {
        System.out.println("从redis提取到购物车数据："+userName);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(userName);
        if(cartList==null){
            cartList=new ArrayList();
        }
        return cartList;
    }
    //将购物车保存到redis
    @Override
    public void saveCartListToRedis(String userName, List<Cart> cartList) {
        System.out.println("向redis中存入购物车数据。。。"+userName);
        redisTemplate.boundHashOps("cartList").put(userName,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_redis, List<Cart> cartList_cookie) {
        System.out.println("合并购物车");
        for(Cart cart:cartList_cookie){
            for(TbOrderItem orderItem:cart.getOrderItemList()){
               cartList_redis =  addGoodsToCartList(cartList_redis,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList_redis;
    }
}
