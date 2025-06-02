package com.justn0w.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @Date: 2025/6/2 18:42
 * @Description:
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DemoServiceTest {

    @Resource
    private DemoService demoService;

    @Test
    public void testProcessChat() {
        log.info("执行单侧类----开始");
        demoService.processChat();
        log.info("执行单侧类----结束");
    }

}
