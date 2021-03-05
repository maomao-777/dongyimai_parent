package com.wzp.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wzp.entity.PageResult;
import com.wzp.mapper.TbSpecificationMapper;
import com.wzp.mapper.TbSpecificationOptionMapper;
import com.wzp.mapper.TbTypeTemplateMapper;
import com.wzp.pojo.TbSpecificationOption;
import com.wzp.pojo.TbSpecificationOptionExample;
import com.wzp.pojo.TbTypeTemplate;
import com.wzp.pojo.TbTypeTemplateExample;
import com.wzp.pojo.TbTypeTemplateExample.Criteria;
import com.wzp.sellergoods.service.TypeTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
    private TbSpecificationMapper specificationMapper;
	@Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;
	/*缓存*/
    @Autowired
    private RedisTemplate redisTemplate;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		return typeTemplateMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);		
/*--------------2------day13缓存品牌和规格列表数据------这样在增删改后会自动调用该方法.---------*/
		saveRedis();//存入数据到缓存

		return new PageResult(page.getTotal(), page.getResult());
	}

    //添加商品获取规格名称以及规格选项数据的json集合


    @Override
    public List<Map> findSpecList(Long id) {
        //根据模板id获取对应模板对象
        TbTypeTemplate tbTypeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
        //从模板对象获取规格属性
        List<Map> list = JSON.parseArray(tbTypeTemplate.getSpecIds(), Map.class);
        //遍历规格集合
        if (list!=null){
            for (Map map : list) {
                //{"id":27,"text":"网络","options":[]}
                Long specid = new Long((Integer) map.get("id"));
                //根据规格id获取规格选项
                TbSpecificationOptionExample example = new TbSpecificationOptionExample();
                TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
                criteria.andSpecIdEqualTo(specid);
                List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample(example);
                //将规格选项存入map集合
                map.put("options",specificationOptionList);
            }
        }
        return list;
    }

  /* ---------------- day13缓存品牌和规格列表数据-------------*/

/**
 * 将数据存入缓存
 */
private void saveRedis(){
    //获取模板集合
    List<TbTypeTemplate> templateList = findAll();
    for (TbTypeTemplate typeTemplate : templateList) {
        //存储品牌列表
        //将品牌字符串转回json集合存的是键值对
        List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(),Map.class);
       redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);
       //存储规格列表
        //根据模板ID查询规格列表、包括规格选项
        List<Map> specList = findSpecList(typeTemplate.getId());
        redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);
    }
    System.out.println("品牌规格放入缓存成功！");
}

}
