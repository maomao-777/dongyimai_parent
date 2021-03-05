package com.wzp.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
//import com.offcn.page.service.ItemPageService;
//import com.offcn.search.service.ItemSearchService;
import com.wzp.entity.PageResult;
import com.wzp.entity.Result;
import com.wzp.group.Goods;
import com.wzp.pojo.TbGoods;
import com.wzp.pojo.TbItem;
import com.wzp.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
   // @Reference
   // private ItemSearchService itemSearchService;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination queueSolrDestination;
    @Autowired
    private Destination queueSolrDeleteDestination;
    @Autowired
    private Destination topicPageDestination;
    @Autowired
    private Destination topicPageDeleteDestination;
  //  @Reference(timeout=40000)
   // private ItemPageService itemPageService;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
             @RequestMapping("/update")
    public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
          // itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
        }
    });
            //删除静态页面
jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
    @Override
    public Message createMessage(Session session) throws JMSException {
        return session.createObjectMessage(ids);
    }
});

			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

    /**
     * 更新状态
     * @param ids
     * @param status
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status){
        try {
            goodsService.updateStatus(ids, status);
            //判断审核状态为审核通过
            if (status.equals("1")) {
                //查询审核通过后的增量SKU数据
                List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(ids, status);
                //完成导入
                if (!CollectionUtils.isEmpty(itemList)) {
                 //   itemSearchService.importList(itemList);
                    final String itemListStr = JSON.toJSONString(itemList);
                    jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(itemListStr);
                        }
                    });
                } else {
                    System.out.println("没有SKU信息");
                }
                //生成静态页面
                for ( final Long id : ids) {
                    //itemPageService.genItemHtml(id);
                    jmsTemplate.send(topicPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                }
            }
            return new Result(true, "成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "失败");
        }
    }

 /*  *//* --------day15发出请求调用生成商品详情的静态页面-----------*//*
	@RequestMapping("/genItemHtml")
    public String genItemHtml(Long goodsId){
	    return "create:"+itemPageService.genItemHtml(goodsId);
    }*/

}
