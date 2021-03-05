package com.offcn.stask;

import com.wzp.mapper.TbSeckillGoodsMapper;
import com.wzp.pojo.TbSeckillGoods;
import com.wzp.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillStaskService {
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/30 * * * * ?")//任务调度定时任务
    public void refreshSeckillGoods() {           //商品的增量缓存
        //2.查询缓存
        Set keys = redisTemplate.boundHashOps("seckillGoods").keys();
        //1.查询数据库
        TbSeckillGoodsExample seckillGoodsExample = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = seckillGoodsExample.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过
        criteria.andStockCountGreaterThan(0);//库存数量大于0
        criteria.andStartTimeLessThan(new Date());//大于开始时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间小于当前时间
        //判断缓存中的商品Id集合是否为空
        if (!CollectionUtils.isEmpty(keys)) {
            //查询数据库新增的Id不在缓存中存在商品
            criteria.andIdNotIn(new ArrayList<Long>(keys));
        }
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(seckillGoodsExample);
//判断新增的商品集合是否为空
        if (!CollectionUtils.isEmpty(seckillGoodsList)) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {//添加到数据库,以商品Id为key值，以商品为value值
                System.out.println("将Id为：" + seckillGoods.getId() + "的商品添加到缓存");
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
            }
        }
    }

    //秒杀商品的下架
    @Scheduled(cron = "0/30 * * * * ?")
    public void removeSeckillGoods() {
        //1.从缓存中取得秒杀商品的集合
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        //判空
        if (!CollectionUtils.isEmpty(seckillGoodsList)) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                //遍历集合
                if (seckillGoods.getEndTime().getTime() < System.currentTimeMillis()) {
                    //同步到数据库
                    seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                    //删除缓存
                    redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
                    System.out.println("在缓存中移除ID：" + seckillGoods.getId() + "的商品");
                }
            }
        }
    }

}
