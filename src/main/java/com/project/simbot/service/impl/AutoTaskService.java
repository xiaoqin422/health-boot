package com.project.simbot.service.impl;

import catcode.CatCodeUtil;
import catcode.Neko;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.project.simbot.service.TaskHealthService;
import com.project.simbot.util.MsgUtil;
import com.project.simbot.util.SendMessageUtil;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.api.message.results.GroupFullInfo;
import love.forte.simbot.api.message.results.GroupMemberInfo;
import love.forte.simbot.api.message.results.GroupMemberList;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.timer.Cron;
import love.forte.simbot.timer.EnableTimeTask;


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
    @Depend
    private TaskHealthService taskHealthService;
    @Depend
    private MsgUtil msgUtil;
    @Depend
    private TaskNotifyServiceImpl taskNotifyService;
    @Cron(value = "0 5 8 * * ?", delay = 1000)
    public void doHealthAutoTask() {
        taskHealthService.autoTask();
    }

    @Cron(value = "0 0 9,10,11 * * ? ")
    public void doNotifyTask(){
        if (taskNotifyService.classIsFinished()){
            msgUtil.sendMsg(SendMessageUtil.getHealthTaskMessageSuccessHeader() + "所有人员打卡任务执行成功！");
        }else {
            taskNotifyService.doAtNotify(false);
        }
    }

    /**
     * 11点未完成打卡，私聊管理员提醒
     */
    @Cron(value = "0 0 11 * * ? ")
    public void t(){
        if (!taskNotifyService.classIsFinished()){
            msgUtil.sendMsg(taskNotifyService.getUnFinishMsg("454062801",false,false).append("\n")
                    .append("---督促可发送指令[软件学院打卡督促]---").toString());
        }
    }
    @Cron(value = "0 30 9 * * ? ")
    public void doNotifyTask_1(){
        if (taskNotifyService.classIsFinished()){
            msgUtil.sendMsg(SendMessageUtil.getHealthTaskMessageSuccessHeader() + "所有人员打卡任务执行成功！");
        }else {
            taskNotifyService.doAtNotify(false);
        }
    }
}