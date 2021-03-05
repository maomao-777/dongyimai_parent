package com.offcn.utils;

import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.wzp.mapper.TbItemMapper;
import com.wzp.pojo.TbItem;
import com.wzp.pojo.TbItemExample;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemList() {
        //到数据库查询出所有审核状态为1的sku数据
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> tbItemList = tbItemMapper.selectByExample(tbItemExample);
        System.out.println("===商品列表===");
        for (TbItem item : tbItemList) {
            //加入动态域{"动态内存"："168","网络"："联通3G"}
            //获取spec将其转为JSON对象格式拼音，且小写
            Map<String,String> specMap=JSON.parseObject(item.getSpec(), Map.class);
            //创建一个map集合用来存储拼音
            Map<String, String> pingyinMap = new HashMap<>();
            for (String key : specMap.keySet()) {
                //将key拼音做拼音转换 item_spec_dongtaineicun
               pingyinMap.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            item.setMap(pingyinMap);
            System.out.println(item.getTitle());
        }
        System.out.println("===结束===");

        //将查询到的数据存到solr
        solrTemplate.saveBeans(tbItemList);
        solrTemplate.commit();
        System.out.println("导入成功");
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItemList();
    }

    /*//删除全部
    @Test
    public void deleteAll(){
        Query query =new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }*/

}
