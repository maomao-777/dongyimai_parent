package com.offcn.seckill.service.impl;

import java.util.Date;
import java.util.List;

import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.seckill.utils.RedisLock;
import com.offcn.utils.IdWorker;
import com.wzp.entity.PageResult;
import com.wzp.mapper.TbSeckillGoodsMapper;
import com.wzp.mapper.TbSeckillOrderMapper;
import com.wzp.pojo.TbSeckillGoods;
import com.wzp.pojo.TbSeckillOrder;
import com.wzp.pojo.TbSeckillOrderExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisLock redisLock;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        TbSeckillOrderExample.Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }
        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 提交订单
     *
     * @param seckillId
     * @param userId
     */
    @Override
    public void submitOrder(Long seckillId, String userId) {
        String appId = "createOrderLock";
        //设置超时时间
        long ex = 1 * 1000L;
        String value = String.valueOf(System.currentTimeMillis() + ex);
        boolean lock = redisLock.lock(appId, value);//设置锁
        if (lock) {
//根据秒杀商品编号从缓存中获取秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
            //判断秒杀商品是否为空
            if (seckillGoods == null) {
                // throw new RuntimeException("秒杀商品不存在");
                System.out.println("秒杀商品不存在");
            }
            //判断商品数量库存是否大于0
            if (seckillGoods.getStockCount() <= 0) {
                //抛出异常结束秒杀提交
//              //throw new RuntimeException("商品已被抢光");
                System.out.println("商品已被抢光");
            }
            //否则扣减库存（Redis）
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            //更新缓存中的商品信息
            redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
            //保存订单到缓存
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(seckillId);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            seckillOrder.setUserId(userId);
            seckillOrder.setStatus("0");//订单状态0
            //保存订单到redis
            redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);

            //判断当缓存中商品库存等于0的时候，把redis缓存的秒杀商品信息保存到数据库
            if (seckillGoods.getStockCount() == 0) {
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                //清除缓存中的秒杀商品
                redisTemplate.boundHashOps("seckillGoods").delete(seckillId);//根据秒杀商品的id删除
            }
//程序执行完毕后，解锁
            redisLock.unlock(appId, value);
        }
    }

    /**
     * 通过当前登录人在缓存中查询订单信息
     *
     * @param userId
     * @return
     */
    @Override
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    //通过当前登录人在缓存中查询登录信息


    @Override
    public void saveOrderFromRedisToDb(String userId, long orderId, String transactionId) {
//根据用户Id从缓存中查询订单
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        //判断订单是否为空
        if (seckillOrder == null) {
            throw new RuntimeException("该订单不存在！");
        }
        //判断订单是否一致防止多线程
        if (seckillOrder.getId().longValue() != orderId) {
            throw new RuntimeException("订单不相符！");
        }
        //设置订单参数
        seckillOrder.setPayTime(new Date());
        seckillOrder.setStatus("1");//已经支付
        seckillOrder.setTransactionId(transactionId);//流水号
        //保存数据库
        seckillOrderMapper.insert(seckillOrder);
        //清空缓存中的订单
        redisTemplate.boundHashOps("seckillOrder").delete(userId);

    }
    /* ---------------------------------秒杀支付业务--------------------------------*/
    //秒杀超时删除订单
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        //1.删除订单，需要根据用户Id在缓存中获取订单
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        //2.从缓存中获取的订单要判空，并判断是否和需要删除的订单是否是同一个Id
        if (null != seckillOrder && seckillOrder.getId().longValue() == orderId.longValue()) {
            //Long对象类型比较，比较的是地址，所以调用方法longvalue
            //3.从缓存中删除该用户的订单
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
            //恢复库存,在秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
            if (null != seckillGoods) {//缓存中存在该商品
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);//修改缓存数量
                System.out.println("现在缓存中数量为：" + seckillGoods.getStockCount());
                redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
            }
        }
    }
}
