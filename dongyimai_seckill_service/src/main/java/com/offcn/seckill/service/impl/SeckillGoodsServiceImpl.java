package com.offcn.seckill.service.impl;

import java.util.Date;
import java.util.List;

import com.wzp.entity.PageResult;
import com.wzp.mapper.TbSeckillGoodsMapper;
import com.wzp.pojo.TbSeckillGoods;
import com.wzp.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.seckill.service.SeckillGoodsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;


/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillGoods> findAll() {
        return seckillGoodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillGoods seckillGoods) {
        seckillGoodsMapper.insert(seckillGoods);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillGoods seckillGoods) {
        seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillGoods findOne(Long id) {
        return seckillGoodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillGoodsMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillGoods seckillGoods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();

        if (seckillGoods != null) {
            if (seckillGoods.getTitle() != null && seckillGoods.getTitle().length() > 0) {
                criteria.andTitleLike("%" + seckillGoods.getTitle() + "%");
            }
            if (seckillGoods.getSmallPic() != null && seckillGoods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + seckillGoods.getSmallPic() + "%");
            }
            if (seckillGoods.getSellerId() != null && seckillGoods.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillGoods.getSellerId() + "%");
            }
            if (seckillGoods.getStatus() != null && seckillGoods.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillGoods.getStatus() + "%");
            }
            if (seckillGoods.getIntroduction() != null && seckillGoods.getIntroduction().length() > 0) {
                criteria.andIntroductionLike("%" + seckillGoods.getIntroduction() + "%");
            }
        }

        Page<TbSeckillGoods> page = (Page<TbSeckillGoods>) seckillGoodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public List<TbSeckillGoods> findList() {
        //从缓存中读取数据
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        if (CollectionUtils.isEmpty(seckillGoodsList)) {
            //创建查询对象
            TbSeckillGoodsExample tbSeckillGoodsExample = new TbSeckillGoodsExample();
            TbSeckillGoodsExample.Criteria criteria = tbSeckillGoodsExample.createCriteria();
            criteria.andStatusEqualTo("1");//查询审核通过的
            criteria.andStartTimeLessThan(new Date());//当前时间大于开始时间
            criteria.andEndTimeGreaterThan(new Date());//当前时间小于结束时间
            seckillGoodsList = seckillGoodsMapper.selectByExample(tbSeckillGoodsExample);
            //从数据库中获取并一个个保存到缓存中
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
            }
        } else {
            System.out.println("从缓存中读取秒杀商品！");
        }
        return seckillGoodsList;
    }

    @Override
    public TbSeckillGoods findOneFromRedis(Long id) {
        return (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
    }
}
