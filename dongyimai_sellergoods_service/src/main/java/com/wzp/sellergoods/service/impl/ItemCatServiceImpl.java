package com.wzp.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wzp.entity.PageResult;
import com.wzp.mapper.TbItemCatMapper;
import com.wzp.mapper.TbTypeTemplateMapper;
import com.wzp.pojo.TbItem;
import com.wzp.pojo.TbItemCat;
import com.wzp.pojo.TbItemCatExample;
import com.wzp.pojo.TbItemCatExample.Criteria;
import com.wzp.pojo.TbTypeTemplate;
import com.wzp.sellergoods.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 商品类目服务实现层
 *
 * @author Administrator
 */
@Service(timeout = 30000)
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;
    //缓存
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbItemCat> findAll() {
        return itemCatMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbItemCat itemCat) {
        itemCatMapper.insert(itemCat);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbItemCat itemCat) {
        itemCatMapper.updateByPrimaryKey(itemCat);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbItemCat findOne(Long id) {
        return itemCatMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            itemCatMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbItemCatExample example = new TbItemCatExample();
        Criteria criteria = example.createCriteria();

        if (itemCat != null) {
            if (itemCat.getName() != null && itemCat.getName().length() > 0) {
                criteria.andNameLike("%" + itemCat.getName() + "%");
            }
        }

        Page<TbItemCat> page = (Page<TbItemCat>) itemCatMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 返回下级列表
     * day13根据上级ID查询列表
     * @param parentId
     * @return
     */
    @Override
    public List<TbItemCat> findByParentId(Long parentId) {
        //实例化查询条件对象
        TbItemCatExample tbItemCatExample = new TbItemCatExample();
        Criteria criteria = tbItemCatExample.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        /*------------------day13----------------------*/
        //缓存商品分类数据
        //每次执行查询的时候，一次性读取缓存进行存储 (因为每次增删改都要执行此方法)
        List<TbItemCat> list = findAll();
        for (TbItemCat itemCat : list) {
            //将数据存入缓存
            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
        }
        System.out.println("更新缓存:商品分类表");
        return itemCatMapper.selectByExample(tbItemCatExample);
    }

    @Override
    public List<TbTypeTemplate> findAllType() {
        return typeTemplateMapper.selectByExample(null);
    }
}
