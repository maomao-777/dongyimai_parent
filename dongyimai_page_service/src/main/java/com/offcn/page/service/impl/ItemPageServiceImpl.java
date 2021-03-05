package com.offcn.page.service.impl;
import com.offcn.page.service.ItemPageService;
import com.wzp.mapper.TbGoodsDescMapper;
import com.wzp.mapper.TbGoodsMapper;
import com.wzp.mapper.TbItemCatMapper;
import com.wzp.mapper.TbItemMapper;
import com.wzp.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {
//注入属性配置文件FreeMarkerConfig
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper tbItemMapper;
    //获取属性文件的值
    @Value("${pagedir}")
    private String pagedir;

    @Override
    public boolean genItemHtml(Long goodsId) {

        try {
            //创建配置对象
            Configuration configuration =freeMarkerConfig.getConfiguration();
            //创建模板对象
            Template template = configuration.getTemplate("item.ftl");
          //创建数据源
           Map dataSoure=new HashMap<String,Object>();
           //查询商品信息
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
            //查询商品扩展信息
            TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            //根据分类id获取分类名称
            TbItemCat itemCat1 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            TbItemCat itemCat2 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            TbItemCat itemCat3 = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            //查询sku信息
            TbItemExample tbItemExample = new TbItemExample();
            TbItemExample.Criteria criteria = tbItemExample.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andGoodsIdEqualTo(goodsId);
            tbItemExample.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(tbItemExample);



            dataSoure.put("goods",tbGoods);
            dataSoure.put("goodsDesc",tbGoodsDesc);
            dataSoure.put("itemCat1",itemCat1);
            dataSoure.put("itemCat2",itemCat2);
            dataSoure.put("itemCat3",itemCat3);
            dataSoure.put("itemList",itemList);

            //文件输出流
            FileWriter out = new FileWriter(pagedir+goodsId+".html");
             //执行生成
            template.process(dataSoure,out);
            //关闭流
            out.close();
             return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void deleteItemHtml(Long goodsId) {
        System.out.println("删除商品的ID"+goodsId);
//删除本地文件
        new File(pagedir+goodsId+".html").delete();
    }
}
