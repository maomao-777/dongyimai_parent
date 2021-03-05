package com.wzp.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.wzp.entity.PageResult;
import com.wzp.group.Specification;
import com.wzp.mapper.TbSpecificationMapper;
import com.wzp.mapper.TbSpecificationOptionMapper;
import com.wzp.pojo.TbSpecification;
import com.wzp.pojo.TbSpecificationExample;
import com.wzp.pojo.TbSpecificationExample.Criteria;
import com.wzp.pojo.TbSpecificationOption;
import com.wzp.pojo.TbSpecificationOptionExample;
import com.wzp.sellergoods.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {
		/*先添加规格表*/
		specificationMapper.insert(specification.getSpecification());
		/*获取添加后的规格id*/
		Long id = specification.getSpecification().getId();
		/*遍历规格选项*/
		for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
			/*根据规格id向规格选项表添加*/
			tbSpecificationOption.setSpecId(id);
			specificationOptionMapper.insert(tbSpecificationOption);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//更新规格管理
		specificationMapper.updateByPrimaryKey(specification.getSpecification());
		//在更新规格选项之前先根据规格id删除之前的规格选项
		TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
		criteria.andSpecIdEqualTo(specification.getSpecification().getId());
		specificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
		//添加规格选项
		for (TbSpecificationOption tbSpecificationOption : specification.getSpecificationOptionList()) {
			tbSpecificationOption.setSpecId(specification.getSpecification().getId());
			specificationOptionMapper.insert(tbSpecificationOption);
		}
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//根据id查询规格
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		//根据规格id查询规格选项
		TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
		criteria.andSpecIdEqualTo(id);
		List<TbSpecificationOption> tbSpecificationOptions = specificationOptionMapper.selectByExample(tbSpecificationOptionExample);
		//返回复合实体
		Specification specification = new Specification();
		specification.setSpecification(tbSpecification);
		specification.setSpecificationOptionList(tbSpecificationOptions);
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//删除规格表
			specificationMapper.deleteByPrimaryKey(id);
			//根据规格id删除规格选项
			TbSpecificationOptionExample tbSpecificationOptionExample = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = tbSpecificationOptionExample.createCriteria();
			criteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(tbSpecificationOptionExample);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}
}
