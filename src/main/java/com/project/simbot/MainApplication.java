package com.project.simbot;

import love.forte.simbot.annotation.SimbotApplication;
import love.forte.simbot.annotation.SimbotResource;
import love.forte.simbot.core.SimbotApp;

/**
 * 包名: com.project.simbot
 * 类名: MainApplication
 * 创建用户: 25789
 * 创建日期: 2022年02月28日 19:30
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@SimbotApplication({
        // 使用配置文件 simbot.yml
        @SimbotResource(value = "simbot.yml"),
})
public class MainApplication {
    public static void main(String[] args) {
        SimbotApp.run(MainApplication.class, args);
    }
}