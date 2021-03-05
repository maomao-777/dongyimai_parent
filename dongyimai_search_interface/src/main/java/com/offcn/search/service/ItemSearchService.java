package com.offcn.search.service;

import com.wzp.pojo.TbItem;

import java.util.List;
import java.util.Map;
//搜索
public interface ItemSearchService {
    public Map<String,Object> search(Map<String,Object>search);
/**
 * 导入数据
 * @param list
 */
public void importList(List<TbItem>list);
    /**
     * 删除数据
     * @param
     */
    public void deleteByGoodsIds(List goodsIdList);


}


