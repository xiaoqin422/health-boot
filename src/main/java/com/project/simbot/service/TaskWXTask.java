package com.project.simbot.service;

/**
 * 包名: com.project.simbot.service
 * 类名: TaskWXTask
 * 创建用户: 25789
 * 创建日期: 2022年03月16日 16:46
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
public interface TaskWXTask {
    /**
     * session会话存活
     * @param sessionID session存活
     */
    public boolean keepLife(String sessionID);

    /**
     * 执行打卡
     * @param session sessionId
     */
    public boolean doTask(String session);
}
