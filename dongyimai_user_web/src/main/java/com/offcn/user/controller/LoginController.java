package com.offcn.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: lhq
 * @Date: 2021/1/6 14:49
 * @Description:
 */
@RestController
public class LoginController {

    @RequestMapping("/getLoginName")
    public Map getLoginName(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("name",name);
        return map;
    }
}
