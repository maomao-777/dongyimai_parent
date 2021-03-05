package com.offcn.seckill.service;
import com.wzp.entity.PageResult;
import com.wzp.pojo.TbSeckillOrder;

import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SeckillOrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSeckillOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSeckillOrder seckill_order);
	
	
	/**
	 * 修改
	 */
	public void update(TbSeckillOrder seckill_order);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSeckillOrder findOne(Long id);
	
	
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
	public PageResult findPage(TbSeckillOrder seckill_order, int pageNum, int pageSize);
    /**
     * 提交订单
     * @param seckillId
     * @param userId
     */
    public void submitOrder(Long seckillId,String userId);

    //通过当前登录人在缓存中查询登录信息
    public TbSeckillOrder searchOrderFromRedisByUserId(String userId);
    //支付成功，将缓存中保存到数据库，并删除缓存中数据transactionId支付宝流水号
    public void saveOrderFromRedisToDb(String userId,long orderId,String transactionId);

    //秒杀超时删除订单
    /*userId登录人 orderId订单Id*/
    public void deleteOrderFromRedis(String userId,Long orderId);

}
