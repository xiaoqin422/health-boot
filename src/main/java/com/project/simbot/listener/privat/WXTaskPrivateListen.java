package com.project.simbot.listener.privat;

import com.project.simbot.service.TaskWXTask;
import com.project.simbot.service.impl.AutoTaskService;
import com.project.simbot.util.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.Filters;
import love.forte.simbot.annotation.Listen;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.filter.MatchType;

import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 包名: com.project.simbot.listener.privat
 * 类名: WXTaskPrivateListen
 * 创建用户: 25789
 * 创建日期: 2022年03月16日 17:01
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans
@Slf4j
public class WXTaskPrivateListen {
    private final List<String> sessions = AutoTaskService.sessions;
    @Depend
    private TaskWXTask taskWXTask;
    @OnPrivate
    @Filters(value = {
            @Filter(value = "企业微信打卡会话存活-", matchType = MatchType.STARTS_WITH)},
            customFilter = {"ManagerFilter"}
    )
    public void keepTask(PrivateMsg msg,Sender sender){
        String msgText = msg.getText();
        String sessionID = msgText.substring(msgText.indexOf('-') + 1);
        String accountCode = msg.getAccountInfo().getAccountCode();
        if (sessions.contains(sessionID)){
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader() + "会话重复提交");
            return;
        }
        sessions.add(sessionID);
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader() + "会话保持存活");
    }
    @OnPrivate
    @Filters(value = {
            @Filter(value = "企业微信打卡会话是否存活-", matchType = MatchType.STARTS_WITH)},
            customFilter = {"ManagerFilter"}
    )
    public void SessionIsAlive(PrivateMsg msg,Sender sender){
        String msgText = msg.getText();
        String sessionID = msgText.substring(msgText.indexOf('-') + 1);
        String accountCode = msg.getAccountInfo().getAccountCode();
        if (sessions.contains(sessionID)){
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader() + "会话存活");
            return;
        }
        sessions.add(sessionID);
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader() + "会话过期");
    }
    @OnPrivate
    @Filters(value = {
            @Filter(value = "企业微信打卡-", matchType = MatchType.STARTS_WITH)},
            customFilter = {"ManagerFilter"}
    )
    public void doTask(PrivateMsg msg, Sender sender){
        String msgText = msg.getText();
        String sessionID = msgText.substring(msgText.indexOf('-') + 1);
        boolean b = taskWXTask.doTask(sessionID);
        if (b){
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader() + "企业微信打卡成功");
        }else {
            sender.sendPrivateMsg(msg,SendMessageUtil.getHealthTaskMessageFailHeader() + "企业微信打卡失败");
        }
    }
}