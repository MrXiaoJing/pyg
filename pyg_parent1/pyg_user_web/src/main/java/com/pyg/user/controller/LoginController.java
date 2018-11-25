package com.pyg.user.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

//获取登陆信息
@RestController
@RequestMapping("/login")
public class LoginController {
    //获取当前登陆名
    @RequestMapping("/findLoginUser")
    public Map findLoginUser(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
    Map map = new HashMap();
    map.put("loginName",name);
    return map;
    }
}
