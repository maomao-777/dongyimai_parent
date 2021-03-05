package com.offcn;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

//任务调度测试类
@Component
public class Task {
    //任务输出当前时间
   // @Scheduled(cron = "5-44 * * * * ?")
    public void test() {
        System.out.println(new Date().toLocaleString());
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext-*.xml");
        Task test = (Task) context.getBean("task");//通过构造方法名获取对对象  name首字母 要小写
       /* test.test();*/
        try {
            System.in.read();  //进程一直开启
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
