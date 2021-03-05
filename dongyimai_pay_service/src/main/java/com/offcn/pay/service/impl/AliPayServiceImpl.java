package com.offcn.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class AliPayServiceImpl implements AliPayService {
    @Autowired
    private AlipayClient alipayClient;

    @Override
    public Map createNative(String out_trade_no, String total_amount) {//订单号和总金额

        Map<String, Object> resultMap = new HashMap<String, Object>();
        //创建预下单请求对象
        //支付金额单位换算  分转元
        Long money = Long.parseLong(total_amount);//字符串转换成长整型
        BigDecimal big = new BigDecimal(money);//长整型转换成短整型；
        BigDecimal cs = new BigDecimal(100L);//分转元单位换算
        BigDecimal total = big.divide(cs);
        try {
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest(); //创建API对应的request类
            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + out_trade_no + "\"," + //商户订单号
                    "\"total_amount\":\"" + total.doubleValue() + "\"," +
                    "\"subject\":\"Iphone6 16G\"," +
                    "\"store_id\":\"NJ_001\"," +
                    "\"timeout_express\":\"90m\"}"); //订单允许的最晚付款时间
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            System.out.println(response.getBody());
            //根据response中的结果继续业务逻辑处理
            String code = response.getCode();//返回响应码
            if (null != code && code.equals("10000")) {
                resultMap.put("qrCode", response.getQrCode());//二维码连接
                resultMap.put("out_trade_no", response.getOutTradeNo());//订单号
                resultMap.put("total_amount", total_amount); //支付金额
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        return resultMap;
    }
    //根据订单编号：查询交易状态
    @Override
    public Map<String, Object> queryPayStatus(String out_trade_no) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();//创建API对应的request类
            request.setBizContent("{" +
                    "\"out_trade_no\":\"" + out_trade_no + "\"," +
                    "\"trade_no\":\"\"}"); //设置业务参数
            AlipayTradeQueryResponse response = alipayClient.execute(request);//通过alipayClient调用API，获得对应的response类
            System.out.print(response.getBody());
//根据response中的结果继续业务逻辑处理
            String code = response.getCode();
            if(null!=code&&code.equals("10000")){
                resultMap.put("status",response.getTradeStatus());//返回交易状态码
                resultMap.put("out_trade_no",response.getOutTradeNo());//交易编号
                resultMap.put("trade_no",response.getTradeNo());//支付平台返回交易流水号
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
