package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbPayLog;
import com.pyg.utils.IdWorker;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class payController {
    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private OrderService orderService;
    //生成二维码
    @RequestMapping("/createNative")
    public Map createNative(){
//        IdWorker out_trade_no = new IdWorker();
//       return weixinPayService.createNative(out_trade_no.nextId()+"","1");
        //获取当前登陆人
       String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis中查找支付日志
       TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        //判断支付日志是否存在
        return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");

    }
    //查询支付状态
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result = null;
        int count=0;
        while(true){
            //调用查询接口
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
            System.out.println(map);
            //判断map是否为null
            if(map==null){
                //支付出错
                result=new Result(false,"支付出错");
                break;
            }
            if("SUCCESS".equals(map.get("trade_state"))){
                //支付成功
                result=new Result(true,"支付成功");
               //修改订单状态
               orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);//间隔3秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if(count>=10){
                result=new Result(false,"TIME_OUT");
                break;
            }
        }

        return result;
    }
}
