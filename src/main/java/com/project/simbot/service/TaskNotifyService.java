package com.project.simbot.service;

import com.project.simbot.entity.Notify;

import java.util.List;
import java.util.Map;

/**
 * 包名: com.project.simbot.service
 * 类名: TaskNotifyService
 * 创建用户: 25789
 * 创建日期: 2022年06月19日 16:48
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
public interface TaskNotifyService {
    boolean classIsFinished();
    Map<String,Object> getUnFinishMsg();
    String getClassFinishMsg();
    void doAtNotify(boolean isNotifySelf);
}