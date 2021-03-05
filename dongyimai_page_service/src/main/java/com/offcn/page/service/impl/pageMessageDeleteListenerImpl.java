package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class pageMessageDeleteListenerImpl implements MessageListener {
   @Autowired
   private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        if (message instanceof ObjectMessage){
            ObjectMessage objectMessage = (ObjectMessage) message;
            try {
                Long[] ids = (Long[]) objectMessage.getObject();
                for (Long id : ids) {
                    itemPageService.deleteItemHtml(id);
                }
                System.out.println("获取消息,删除页面成功");

            } catch (JMSException e) {
                e.printStackTrace();
            }

        }
    }
}
