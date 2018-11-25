package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import com.pyg.pojogroup.Cart;
import com.pyg.utils.CookieUtil;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    //购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //得到登陆人账号，判断当前是否有人登陆
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartList = CookieUtil.getCookieValue(request, "cartList", "utf-8");
        if (cartList==null||cartList.equals("")){
            cartList="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartList,Cart.class);
        if("anonymousUser".equals(userName)){
            //没登录，读取cookie中的
            return cartList_cookie;
        }else{
            //登录了
            List<Cart> cartList_redis = cartService.findCartListFromRedis(userName);//从redis中提取
            if (cartList_cookie.size()>0){
                //合并购物车
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
            //清除cookie数据
                CookieUtil.deleteCookie(request,response,"cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(userName,cartList_redis);
            }
            return cartList_redis;
        }

    }
    //添加商品到购物车
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105")
    public Result addGoodsToCartList(Long itemId,Integer num){
        try{
            //处理js跨域调用的问题
//            response.setHeader("Access-Control-Allow-Origin","http://localhost:9105"); //允许谁来跨域访问
//            response.setHeader("Access-Control-Allow-Credentials","true"); //允许访问的时候带来cookie
            List<Cart> cartList = findCartList(); //获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList,itemId,num);
            //得到登陆人账号，判断当前是否有人登陆
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            if("anonymousUser".equals(userName)) {
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("向cookie存入数据");
            }else{
                //登陆成功，向redis存储
                cartService.saveCartListToRedis(userName,cartList);
            }
            return new Result(true, "添加成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }
}
