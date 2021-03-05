package com.wzp.shop.service;


import com.alibaba.dubbo.config.annotation.Reference;
import com.wzp.pojo.TbSeller;
import com.wzp.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //获取商家对象
        TbSeller tbSeller = sellerService.findOne(username);
        //构建角色列表
        ArrayList<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        if (tbSeller != null){
            if (tbSeller.getStatus().equals("1")){
                return new User(username,tbSeller.getPassword(),list);
            }else {
                return null;
            }
        }
        return null;
    }
}
