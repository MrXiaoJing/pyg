package com.pyg.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbSeckillOrder;
import com.pyg.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class payController {
    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private SeckillOrderService seckillOrderService;

    //生成二维码
    @RequestMapping("/createNative")
    public Map createNative(){
//        IdWorker out_trade_no = new IdWorker();
//       return weixinPayService.createNative(out_trade_no.nextId()+"","1");
        //获取当前登陆人
//       String userId = SecurityContextHolder.getContext().getAuthentication().getName();
//        //到redis中查找支付日志
//       TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
//        //判断支付日志是否存在
//        return weixinPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        //获取当前登录人
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //从redis中查询秒杀订单
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFormRedisByUserId(userId);
        //判断秒杀是否存在
        if(seckillOrder!=null){
            long talFee = (long) (seckillOrder.getMoney().doubleValue()*100);
            return weixinPayService.createNative(seckillOrder.getId()+"",talFee+"");
        }
        return null;

    }
    //查询支付状态
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获取当前登陆人
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int count=0;
        while(true) {
            //调用查询接口
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
            System.out.println(map);
            //判断map是否为null
            if (map == null) {
                //支付出错
                result = new Result(false, "支付出错");
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))) {
                //支付成功
                result = new Result(true, "支付成功");
                //支付成功，调用服务层
                seckillOrderService.searchOrderFromRedisToDb(userId, map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);//间隔3秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if (count >= 10) {
                result = new Result(false, "TIME_OUT");
                //1.调用微信的关闭订单接口
                Map<String, String> payresult = weixinPayService.closePay(out_trade_no);
                if (!"SUCCESS".equals(payresult.get("result_code"))) {//如果返回结果是正常关闭
                    if ("ORDERPAID".equals(payresult.get("err_code"))) {
                        result = new Result(true, "支付成功");
                        seckillOrderService.searchOrderFromRedisToDb(userId, map.get("transaction_id"));
                    }
                }
                if (!result.isSuccess()) {
                    System.out.println("已超时，订单取消");
                    //调用服务层，关闭微信订单，删除订单
                    seckillOrderService.deleteOrderFromRedis(userId, Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;
    }
}
