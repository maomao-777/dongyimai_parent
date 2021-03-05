package com.offcn.page.service.impl;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class PageMessageListenerImpl implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage textMessage = (TextMessage) message;
            try {
                String text = textMessage.getText();
                long id = Long.parseLong(text);//字符串转换为长整型
                System.out.println("接收的商品的ID："+id);
                itemPageService.genItemHtml(id);
                System.out.println("接收消息队列成功，完成静态页面的生成");
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
