package com.offcn.pay.service;

import java.util.Map;

public interface AliPayService {
    //生成支付宝二维码 根据订单号 和金额
    public Map createNative(String out_trade_no, String total_amount);

    //根据订单编号：查询交易状态
    public Map<String,Object>queryPayStatus(String out_trade_no);
}
