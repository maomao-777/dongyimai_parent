package com.offcn.search.service.impl;

import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.Serializable;
import java.util.Arrays;

@Component
public class searchMessageDeleteListenerImpl implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        if(message instanceof ObjectMessage){
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
            Long [] ids =(Long[]) objectMessage.getObject();
                System.out.println("删除元素的ID"+ids);
            //数组转集合
            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
                System.out.println("接收消息队列，完成删除！");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
