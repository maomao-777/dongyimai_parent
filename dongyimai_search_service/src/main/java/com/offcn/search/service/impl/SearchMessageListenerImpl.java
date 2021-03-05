package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.search.service.ItemSearchService;
import com.wzp.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class SearchMessageListenerImpl implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        //类型转换
        TextMessage textMessage = (TextMessage) message;
        try {
            //json字符串转换为对象
            List<TbItem> tbItems = JSON.parseArray(textMessage.getText(), TbItem.class);
            //调用服务完成solr导入
                itemSearchService.importList(tbItems);
            System.out.println("接收消息队列成功，完成导入操作！");


        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
