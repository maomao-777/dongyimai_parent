package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.order.service.OrderService;
import com.offcn.pay.service.AliPayService;
import com.offcn.utils.IdWorker;
import com.wzp.entity.Result;
import com.wzp.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/alipay")
public class AliPayController {
    @Reference(timeout = 80000)
    private AliPayService aliPayService;
   @Reference(timeout = 80000)
    private OrderService orderService;
    @Autowired
    private IdWorker idWorker;

    @RequestMapping("/createNative")
    public Map<String, Object> createNative() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //从缓存中读取支付日志
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
       //缓存中需要判空
        if(null!=payLog){
            return aliPayService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        }else {
            return new HashMap<String,Object>();
        }

    }

    //根据订单编号：查询交易状态
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo) {
        Result result = null;
        int x = 0;
        while (true) {
            Map map = null;
            try {
                map = aliPayService.queryPayStatus(outTradeNo);
            } catch (Exception e) {
                System.out.println("交易失败");
            }
            if (map == null) {
                result = new Result(false, "支付出错");
                break;
            }
            if (null != map.get("status") && map.get("status").equals("TRADE_SUCCESS")) {
                orderService.updateOrderStatus((String)map.get("out_trade_no"),(String)map.get("trade_no"));
                result = new Result(true, "交易支付成功");
                break;
            }
            if (null != map.get("status") && map.get("status").equals("TRADE_CLOSED")) {
                result = new Result(true, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if (null != map.get("status") && map.get("status").equals("TRADE_FINISHED")) {
                result = new Result(true, "交易结束，不可退款");
                break;
            }
            try {
                Thread.sleep(3000);//每隔三秒休眠一次
                x++;
                if (x>=10){
                    result=new Result(false,"二维码超时");
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
