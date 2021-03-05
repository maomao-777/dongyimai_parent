package com.offcn.content.service.impl;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.content.service.ContentService;
import com.wzp.entity.PageResult;
import com.wzp.mapper.TbContentMapper;
import com.wzp.pojo.TbContent;
import com.wzp.pojo.TbContentExample;
import com.wzp.pojo.TbContentExample.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
    private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
	    contentMapper.insert(content);
	    //添加后清空缓存
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        System.out.println("清空了缓存");
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//修改可能修改成其他品牌的分类
        //先到数据库获取修改之前（也是保存之前）的分类ID
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        //根据分类Id清空该分类的缓存
        redisTemplate.boundHashOps("content").delete(categoryId);
        contentMapper.updateByPrimaryKey(content);
        //到缓存中<content.getCategoryId().longValue()>查询分类id判断前后是否修改分类id
        if (content.getCategoryId().longValue()!=categoryId.longValue()){
            //不等说明修改了分类，再次清空新的分类缓存
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
		    //根据在数据库中查询分类id
            Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();
            redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	//轮播图


    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
	    //根据分类ID到缓存中查询广告集合
        List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);//content作为key值categoryId当id索引
//如果集合数据为空则到数据库中查询，并将集合保存到数据库中
        if (CollectionUtils.isEmpty(contentList)){
            TbContentExample tbContentExample = new TbContentExample();
            Criteria criteria = tbContentExample.createCriteria();
            //设置查询条件
            criteria.andCategoryIdEqualTo(categoryId);
            criteria.andStatusEqualTo("1");    //有效
            tbContentExample.setOrderByClause("sort_order");   //默认 升序排序
            contentList=contentMapper.selectByExample(tbContentExample);
            //同步缓存
            redisTemplate.boundHashOps("content").put(categoryId,contentList);
        }else {
            System.out.println("从缓存中读取数据");
        }
        return contentList;
    }
}
