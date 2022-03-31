package com.project.simbot.service.impl;

import com.project.simbot.service.TaskHealthService;
import com.project.simbot.service.TaskWXTask;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.timer.Cron;
import love.forte.simbot.timer.EnableTimeTask;

import java.util.ArrayList;
import java.util.List;

/**
 * 包名: com.project.simbot.service.impl
 * 类名: AutoTaskService
 * 创建用户: 25789
 * 创建日期: 2022年03月01日 17:23
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@EnableTimeTask
@Beans
public class AutoTaskService {
    public static List<String> sessions = new ArrayList<>();
    @Depend
    private TaskHealthService taskHealthService;
    @Depend
    private TaskWXTask taskWXTask;

    @Cron(value = "0 5 8 * * ?", delay = 1000)
    public void doHealthAutoTask() {
        taskHealthService.autoTask();
    }

    @Cron(value = "0 15 * * * ? *")
    public void keepSessions(){
        sessions.removeIf(session -> !taskWXTask.keepLife(session));
    }
}