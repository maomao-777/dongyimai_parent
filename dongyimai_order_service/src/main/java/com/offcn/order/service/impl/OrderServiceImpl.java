package com.offcn.order.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.offcn.utils.IdWorker;
import com.wzp.entity.Cart;
import com.wzp.entity.PageResult;
import com.wzp.mapper.TbOrderItemMapper;
import com.wzp.mapper.TbOrderMapper;
import com.wzp.mapper.TbPayLogMapper;
import com.wzp.pojo.TbOrder;
import com.wzp.pojo.TbOrderExample;
import com.wzp.pojo.TbOrderItem;
import com.wzp.pojo.TbPayLog;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.order.service.OrderService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TbOrderMapper orderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;
    @Autowired
    private TbPayLogMapper payLogMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbOrder> findAll() {
        return orderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbOrder order) {
        //添加购物信息到数据库,需要添加两张表,订单表和订单详情表
        //订单表的一些数据比如订单生成时间等数据需要在后端生成
        //用户登录后需要从缓存中获取数据保存到数据库中
        //1.在缓存中取得购物车列表,通过当前登录人的Id
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
        //2.遍历购物车获取一个个商家购物车（订单对象）,并设置参数
        if (!CollectionUtils.isEmpty(cartList)) {
            double total_fee = 0.00;//设置总金额在支付日志中用
           List<String>orderList=new ArrayList<String>();
            for (Cart cart : cartList) {
                TbOrder tbOrder = new TbOrder();//生成订单对象
                long orderId = idWorker.nextId();// 获取订单id（分布式id）
                System.out.println("orderId :" + orderId);
                //设置订单数据库表
                tbOrder.setOrderId(orderId);//设置订单id
                orderList.add(tbOrder.getOrderId()+"");
                tbOrder.setPaymentType(order.getPaymentType());//表单获取
                tbOrder.setStatus("1");//状态：1、未付款，2、已付款，3、未发货，4、已发货，5、交易成功，6、交易关闭,7、待评价
                tbOrder.setCreateTime(new Date());//创建时间
                tbOrder.setUpdateTime(new Date());//修改时间
                tbOrder.setUserId(order.getUserId());//登录人Id
                tbOrder.setReceiverAreaName(order.getReceiverAreaName());//收件人地址
                tbOrder.setReceiverMobile(order.getReceiverMobile());//收件人电话
                tbOrder.setReceiver(order.getReceiver());//收货人
                tbOrder.setSourceType(order.getSourceType());//订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
                tbOrder.setSellerId(cart.getSellerId());//通过商家购物车获取商家Id
                //获取总金额
                double money = 0.00;
                //3.遍历订单详情列表，保存详情对象
                for (TbOrderItem orderItem : cart.getOrderItemList()) {
                    orderItem.setId(idWorker.nextId());//订单详情Id
                    orderItem.setOrderId(orderId);//订单id（商家购物车）建立关联
                    money += orderItem.getTotalFee().doubleValue();//计算订单实付金额
                    tbOrderItemMapper.insert(orderItem);
                }
                tbOrder.setPayment(new BigDecimal(money));//设置实付金额
                total_fee+=tbOrder.getPayment().doubleValue();//支付日志总金额
                orderMapper.insert(tbOrder);  //保存订单对象
            }
            //判断支付方式是线上支付,创建支付日志
            //设置支付日志参数
            //保存到数据库
            //将支付日志放入缓存
            //判断支付方式是线上支付，创建支付日志
            if (order.getPaymentType().equals("1")) {
                //设置支付日志参数
                TbPayLog tbPayLog = new TbPayLog();
                tbPayLog.setOutTradeNo(idWorker.nextId() + "");         //交易编号
                tbPayLog.setCreateTime(new Date());                     //创建时间

                BigDecimal bigMoney = new BigDecimal(total_fee);
                BigDecimal cs = new BigDecimal(100L);
                BigDecimal totalFee_big = bigMoney.multiply(cs);
                tbPayLog.setTotalFee(totalFee_big.longValue());         //分
                tbPayLog.setUserId(order.getUserId());                  //当前登录人 支付人信息
                String orderListStr = orderList.toString().replace("[", "").replace("]", "").replace(" ", "");
                tbPayLog.setOrderList(orderListStr);
                tbPayLog.setPayType(order.getPaymentType());            //支付类型
                tbPayLog.setTradeState("0");                            //交易状态 未支付
                //保存到数据库
                payLogMapper.insert(tbPayLog);
                //将支付日志放入缓存
                redisTemplate.boundHashOps("payLog").put(order.getUserId(), tbPayLog);
            }

            //4.清空当前登录人的缓存中的购物车列表
            redisTemplate.boundHashOps("cartList").delete(order.getUserId());
        }

        //orderMapper.insert(order);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbOrder order) {
        orderMapper.updateByPrimaryKey(order);
    }

    /**
     * 根据ID获取实体
     *
     * @param
     * @return
     */
    @Override
    public TbOrder findOne(Long orderId) {
        return orderMapper.selectByPrimaryKey(orderId);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] orderIds) {
        for (Long orderId : orderIds) {
            orderMapper.deleteByPrimaryKey(orderId);
        }
    }


    @Override
    public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbOrderExample example = new TbOrderExample();
        TbOrderExample.Criteria criteria = example.createCriteria();

        if (order != null) {
            if (order.getPaymentType() != null && order.getPaymentType().length() > 0) {
                criteria.andPaymentTypeLike("%" + order.getPaymentType() + "%");
            }
            if (order.getPostFee() != null && order.getPostFee().length() > 0) {
                criteria.andPostFeeLike("%" + order.getPostFee() + "%");
            }
            if (order.getStatus() != null && order.getStatus().length() > 0) {
                criteria.andStatusLike("%" + order.getStatus() + "%");
            }
            if (order.getShippingName() != null && order.getShippingName().length() > 0) {
                criteria.andShippingNameLike("%" + order.getShippingName() + "%");
            }
            if (order.getShippingCode() != null && order.getShippingCode().length() > 0) {
                criteria.andShippingCodeLike("%" + order.getShippingCode() + "%");
            }
            if (order.getUserId() != null && order.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + order.getUserId() + "%");
            }
            if (order.getBuyerMessage() != null && order.getBuyerMessage().length() > 0) {
                criteria.andBuyerMessageLike("%" + order.getBuyerMessage() + "%");
            }
            if (order.getBuyerNick() != null && order.getBuyerNick().length() > 0) {
                criteria.andBuyerNickLike("%" + order.getBuyerNick() + "%");
            }
            if (order.getBuyerRate() != null && order.getBuyerRate().length() > 0) {
                criteria.andBuyerRateLike("%" + order.getBuyerRate() + "%");
            }
            if (order.getReceiverAreaName() != null && order.getReceiverAreaName().length() > 0) {
                criteria.andReceiverAreaNameLike("%" + order.getReceiverAreaName() + "%");
            }
            if (order.getReceiverMobile() != null && order.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + order.getReceiverMobile() + "%");
            }
            if (order.getReceiverZipCode() != null && order.getReceiverZipCode().length() > 0) {
                criteria.andReceiverZipCodeLike("%" + order.getReceiverZipCode() + "%");
            }
            if (order.getReceiver() != null && order.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + order.getReceiver() + "%");
            }
            if (order.getInvoiceType() != null && order.getInvoiceType().length() > 0) {
                criteria.andInvoiceTypeLike("%" + order.getInvoiceType() + "%");
            }
            if (order.getSourceType() != null && order.getSourceType().length() > 0) {
                criteria.andSourceTypeLike("%" + order.getSourceType() + "%");
            }
            if (order.getSellerId() != null && order.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + order.getSellerId() + "%");
            }
        }

        Page<TbOrder> page = (Page<TbOrder>) orderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }
    //从缓存中读取当前登录人的支付日志
    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    //修改订单状态

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
//修改支付日志状态
        TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        tbPayLog.setPayTime(new Date());//支付时间
        tbPayLog.setTradeState("1");//支付状态，已支付
        tbPayLog.setTransactionId(transaction_id);//交易流水号
        payLogMapper.updateByPrimaryKey(tbPayLog);
        //修改订单支付状态
        String orderList = tbPayLog.getOrderList();//订单集合111,222,333
        String[] orderIds = orderList.split(",");
        for (String orderId : orderIds) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            tbOrder.setPaymentTime(new Date());//付款时间
            orderMapper.updateByPrimaryKey(tbOrder);
        }
        //清空缓存
        redisTemplate.boundHashOps("payLog").delete(tbPayLog.getUserId());
    }
}
