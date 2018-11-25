package com.pyg.pay.service;

import java.util.Map;

//微信支付接口
public interface WeixinPayService {
    //生成微信支付二维码
    public Map createNative(String out_trade_no,String total_fee);

    //查询支付状态(商户订单号)
    public Map queryPayStatus(String out_trade_no);
    //关闭支付
    Map<String,String> closePay(String out_trade_no);
}
