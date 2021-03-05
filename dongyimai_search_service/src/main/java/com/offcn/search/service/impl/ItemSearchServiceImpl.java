package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.search.service.ItemSearchService;
import com.wzp.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        //创建Map集合接收多模块中查询到的模块数据
        Map resultMap = new HashMap();
        /* -----------------------------高亮查询day13注掉----------------------------*/
        /*//创建查询条件对选哪个
        Query query = new SimpleQuery();
        //创建条件选择器 复制域item_keywords is表示中文分词 查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //装回查询对象
        query.addCriteria(criteria);
        //执行查询
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        //获取分页数据
        List<TbItem> itemList = page.getContent();
        //获取的数据存入多模块集合中*/
//处理查询条件中间有空格
        if (!StringUtils.isEmpty(searchMap.get("keywords")) && ((String) searchMap.get("keywords")).indexOf(" ") > -1) {
            String keywords = ((String) searchMap.get("keywords")).replace(" ", "");//除去中间空格
            searchMap.put("keywords", keywords);
        }


        //1.调用私有方法高亮显示的方式查询列表
        resultMap.putAll(this.searchList(searchMap));

        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        if (!CollectionUtils.isEmpty(categoryList)) {
            resultMap.put("categoryList", categoryList);
        }

        //3.查询品牌和规格列表

        //根据选中的分类进行查询不默认第一个分类
        //获取分类
        String category = (String) searchMap.get("category");
        if (!StringUtils.isEmpty(category)) {
            resultMap.putAll(this.searchBrandAndSpecList(category));

        } else {
            if (categoryList.size() > 0) {
                resultMap.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
            }
        }
        return resultMap;
    }

    /*------------------------day13-----------------------------------------*/
    //一、创建私有方法，用于返回查询列表的结果（高亮）根据关键字查询，对查询的结果进行高亮
    private Map searchList(Map searchMap) {
        //把带高亮数据集合存放map
        Map map = new HashMap();
        //1.创建一个支持高亮查询的查询对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2.设定需要高亮查询的字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //3.设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //4.设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //5.关联高亮选项到查询对象
        query.setHighlightOptions(highlightOptions);
        //6.设置查询条件，根据关键字查询
        //创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria);
        /*--------------------------day13精查询-----------------------*/

        //根据分类筛选结果
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            //设置查询条件
            Criteria categoryCriteria = new Criteria("item_category").is(searchMap.get("category"));
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery();
            filterQuery.addCriteria(categoryCriteria);
            query.addFilterQuery(filterQuery);
        }
        //根据品牌筛选结果
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            //设置查询条件
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            //创建过滤查询对象
            FilterQuery filterQuery = new SimpleFilterQuery();
            filterQuery.addCriteria(brandCriteria);
            query.addFilterQuery(filterQuery);
        }
        //根据规格筛选结果
        if (null != searchMap.get("spec")) {
            //{"网络":"移动2G","机身内存":"64G"}
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                Criteria specCriteria = new Criteria("item_spec_" + Pinyin.toPinyin(key, "").toLowerCase()).is(specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery();
                filterQuery.addCriteria(specCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        /*-------------day14--------------*/
        //根据价格筛选结果
        if (!StringUtils.isEmpty(searchMap.get("price"))) {
            // 500-1000获取价格字符串
            String price = (String) searchMap.get("price");
            String[] priceArr = price.split("-");
            if (!priceArr[0].equals("0")) {//如果区间起点不等于0
                Criteria criteria1 = new Criteria("item_price").greaterThan(priceArr[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);
            }
            if (!priceArr[1].equals("*")) {//如果区间终点不等于*
                Criteria criteria1 = new Criteria("item_price").lessThanEqual(priceArr[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteria1);
                query.addFilterQuery(filterQuery);

            }
        }
        /*分页查询*/
        //当前页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        if (pageNo == null) {
            pageNo = 1;
        }
        //每页显示条数
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null) {
            pageSize = 20;//设置默认值
        }

        //设置起始页和查询的记录数
        query.setOffset((pageNo - 1) * pageSize);
        query.setRows(pageSize);

        /*   -----------------------day14an查询条件排序-------------------*/
        String sortField = (String) searchMap.get("sortField");//排序字段
        String sortValue = (String) searchMap.get("sortValue");//排序规则
        if (!StringUtils.isEmpty(sortField)) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }


        //7.发出带高亮数据查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //8.获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //9.遍历高亮集合
        for (HighlightEntry<TbItem> highlightEntry : highlightEntryList) {
            //获取SKU对象
            TbItem tbItem = highlightEntry.getEntity();
            //取出高亮结果之前进行判空操作
            if (highlightEntry.getHighlights().size() > 0 && highlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                //高亮结果集合
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                //  snipplets 高亮结果集字段<em ..></em>集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果，设置对应到商品标题
                tbItem.setTitle(snipplets.get(0));
            }
        }
        //将查询结果集赋给rows
        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }
    //二、我们今天要完成的目标是在关键字搜索的基础上添加面板搜索功能,面板上有商品分类、品牌、各种规格和价格区间等条件
    // 根据搜索关键字查询商品分类名称列表

    /**
     * 查询分类列表
     *
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap) {
        List list = new ArrayList();
        //创建查询对象缓存中
        Query query = new SimpleQuery();
        //创建条件过滤器
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //将过滤器加入查询对象
        query.addCriteria(criteria);
        //设置分组  根据分类字段
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //设置到查询对象
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组的结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }

    /*---------三、day13在搜索面板区域显示第一个分类的品牌和规格列表-------*/

    /**
     * 到缓存里查询品牌和规格列表
     *
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map<String, Object> map = new HashMap<String, Object>();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//通过品牌名到缓存分类表获取模板ID
        if (typeId != null) {
            //根据模板ID查询品牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);
            //根据模板ID查询规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }


    @Override
    public void importList(List<TbItem> list) {
        for (TbItem item : list) {
            //加入动态域{"动态内存"："168","网络"："联通3G"}
            //获取spec将其转为JSON对象格式拼音，且小写
            Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
            //创建一个map集合用来存储拼音
            Map<String, String> pingyinMap = new HashMap<>();
            for (String key : specMap.keySet()) {
                //将key拼音做拼音转换 item_spec_dongtaineicun
                pingyinMap.put(Pinyin.toPinyin(key, "").toLowerCase(), specMap.get(key));
            }
            item.setMap(pingyinMap);
            System.out.println(item.getTitle());
        }
        System.out.println("===结束===");

        //将查询到的数据存到solr
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
        System.out.println("导入成功");
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品的ID" + goodsIdList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
