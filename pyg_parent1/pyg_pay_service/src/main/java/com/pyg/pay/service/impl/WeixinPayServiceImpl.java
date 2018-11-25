package com.pyg.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.utils.HttpClient;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private  String appid;
    @Value("${partner}")
    private  String partner;
    @Value("${partnerkey}")
    private  String partnerkey;
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.创建参数
        Map<String,String> param = new HashMap<>();//创建参数
        param.put("appid",appid);//公众号
        param.put("mch_id",partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body","品优购");//商品描述
        param.put("out_trade_no",out_trade_no);//商户订单号
        param.put("total_fee",total_fee);//总金额
        param.put("spbill_create_ip","192.168.25.128");//ip
        param.put("notify_url","http://www.jd.com");//回调地址
        param.put("trade_type","NATIVE");

        try {
            //2.生成要发送的xml
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(signedXml);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(signedXml);
            client.post();
            //获取结果
            String content = client.getContent();
            System.out.println(content);
            Map<String, String> xmlToMap = WXPayUtil.xmlToMap(content);
            Map<String,String> map = new HashMap<>();
            map.put("code_url",xmlToMap.get("code_url"));//支付地址
            map.put("total_fee",total_fee);//总金额
            map.put("out_trade_no",out_trade_no);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }
    //查询支付状态
    @Override
    public Map queryPayStatus(String out_trade_no) {
        //创建参数
        Map<String,String> param = new HashMap<>();
        param.put("appid",appid);//公众号id
        param.put("mch_id",partner);//商户号
        param.put("out_trade_no",out_trade_no);//订单号
        param.put("nonce_str",WXPayUtil.generateNonceStr());//随机字符串
        //发送xml
        try {
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            String url="https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();
            //获取结果
            String result = httpClient.getContent();
            System.out.println("响应结果："+result);
            //将result xml转成map
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println("map:"+map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, String> closePay(String out_trade_no) {
        Map param = new HashMap();
        param.put("appid",appid);//公众号
        param.put("mch_id",partner);//商户号
        param.put("out_trade_no",out_trade_no);//订单号
        param.put("nonce_str",WXPayUtil.generateNonceStr());//随机字符串
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";
        try {
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
           httpClient.post();
            String content = httpClient.getContent();
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);
            System.out.println(stringMap);
            return stringMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
