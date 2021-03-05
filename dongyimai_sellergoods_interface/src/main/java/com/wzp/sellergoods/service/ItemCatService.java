package com.wzp.sellergoods.service;

import com.wzp.entity.PageResult;
import com.wzp.pojo.TbItem;
import com.wzp.pojo.TbItemCat;
import com.wzp.pojo.TbTypeTemplate;

import java.util.List;

/**
 * 商品类目服务层接口
 * @author Administrator
 *
 */
public interface ItemCatService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbItemCat> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);


	/**
	 * 增加
	*/
	public void add(TbItemCat item_cat);


	/**
	 * 修改
	 */
	public void update(TbItemCat item_cat);


	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbItemCat findOne(Long id);


	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long[] ids);

	/**
	 * 分页
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbItemCat item_cat, int pageNum, int pageSize);

	/**
	 * 返回下级列表
	 * @param parentId
	 * @return
	 */
	public List<TbItemCat> findByParentId(Long parentId);

	/**
	 * 查询所有模板
	 * @return
	 */
	public List<TbTypeTemplate> findAllType();
}
