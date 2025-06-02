package com.justn0w.controller;

import com.justn0w.service.DemoService;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;

/**
 * @Date: 2025/6/2 18:40
 * @Description:
 */
@Controller
public class ChatController {

    @Resource
    private DemoService demoService;

    @GetMapping("/hello")
    public void hello() {
        demoService.processChat();
        System.out.println("hello world");
    }
}
