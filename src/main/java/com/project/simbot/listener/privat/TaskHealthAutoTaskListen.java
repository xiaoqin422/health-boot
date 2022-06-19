package com.project.simbot.listener.privat;

import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.mchange.v2.resourcepool.TimeoutException;
import com.project.simbot.entity.TaskHealth;
import com.project.simbot.service.TaskHealthService;
import com.project.simbot.util.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.ContinuousSessionScopeContext;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.SessionCallback;

/**
 * 包名: com.project.simbot.listener
 * 类名: TaskHealthAutoTaskListen
 * 创建用户: 25789
 * 创建日期: 2022年03月01日 16:55
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans
@Slf4j
public class TaskHealthAutoTaskListen {
    private static final String GROUP_PID = "Health_AutoTask_PID";
    private static final String GROUP_ADDRESS = "Health_AutoTask_ADDRESS";
    private static final String REGEX_ADDRESS = ".+(省|市).+(市|区).+(区|县).*$";
    private static final String REGEX_PID = "\\d{6}(18|19|20)\\d{2}(0\\d|10|11|12)([0-2]\\d|30|31)\\d{3}[\\dXx]$";
    @Depend
    private TaskHealthService taskHealthService;


    /**
     * 此监听函数监听一个私聊消息，并会复读这个消息，然后再发送一个表情。
     * 此方法上使用的是一个模板注解{@link OnPrivate}，
     * 其代表监听私聊。
     * 由于你监听的是私聊消息，因此参数中要有个 {@link PrivateMsg} 来接收这个消息实体。
     * <p>
     * 其次，由于你要“复读”这句话，因此你需要发送消息，
     * 因此参数中你需要一个 "消息发送器" {@link Sender}。
     * <p>
     * 当然，你也可以使用 {@link love.forte.simbot.api.sender.MsgSender}，
     * 然后 {@code msgSender.SENDER}.
     */
    @OnPrivate
    @Filter(value = "软件学院打卡信息绑定", matchType = MatchType.EQUALS)
    public void replyPrivateMsg1(PrivateMsg privateMsg, ListenerContext context, Sender sender) {
        // 获取私聊持续上下文环境
        ContinuousSessionScopeContext sessionContext =
                (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        // 处理sessionContext为null
        assert sessionContext != null;
        String accountCode = privateMsg.getAccountInfo().getAccountCode();
        sender.sendPrivateMsg(privateMsg, "居民身份证号码");
        final SessionCallback<String> callback = SessionCallback.<String>builder().onResume(pid -> {
            sender.sendPrivateMsg(privateMsg, "签到地址【具体到区/县一级】");
            sessionContext.waiting(GROUP_ADDRESS, accountCode, SessionCallback.<String>builder().onResume(address -> {
                String result = taskHealthService.creatTask(accountCode, address, pid);
                sender.sendPrivateMsg(privateMsg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
            }).onErrorAndCancel(e -> {
                sender.sendPrivateMsg(privateMsg, SendMessageUtil.getHealthTaskMessageFailHeader().append("打卡信息绑定失败:").append(e).toString());
            }).build());

        }).onError(e -> {
            if (e instanceof TimeoutException) {
                log.error("会话超时" + e);
            } else {
                log.error("会话出错" + e);
            }
        }).onCancel(e -> {
            log.error("会话关闭" + e);
        }).build(); // build 构建
        sessionContext.waiting(GROUP_PID, accountCode, callback);
    }

    @OnPrivate
    @OnlySession(group = GROUP_PID)
    @Filter(value = REGEX_PID, matchType = MatchType.REGEX_MATCHES)
    public void pid(PrivateMsg msg, ListenerContext context) {
        // 获取私聊持续上下文环境
        ContinuousSessionScopeContext sessionContext =
                (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        assert sessionContext != null;
        String pid = msg.getText();
        sessionContext.push(GROUP_PID, msg.getAccountInfo().getAccountCode(), pid);
    }

    @OnPrivate
    @OnlySession(group = GROUP_ADDRESS)
    @Filter(value = REGEX_ADDRESS, matchType = MatchType.REGEX_FIND)
    public void address(PrivateMsg msg, ListenerContext context) {
        // 获取私聊持续上下文环境
        ContinuousSessionScopeContext sessionContext =
                (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        assert sessionContext != null;
        sessionContext.push(GROUP_ADDRESS, msg.getAccountInfo().getAccountCode(), msg.getText());
    }

    @OnPrivate
    @Filter(value = "软件学院打卡修改打卡地址-{{address,.+(省|市).+(市|区).+(区|县).*$}}", matchType = MatchType.REGEX_MATCHES)
    public void updateTask(PrivateMsg msg, Sender sender, @FilterValue("address") String address) {
        boolean match = ReUtil.isMatch(".+(省|市).+(市|区).+(区|县).*$", address);
        if (match) {
            String result = taskHealthService.updateTaskAddress(msg.getAccountInfo().getAccountCode(), address);
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
        } else {
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageFailHeader().append("打卡地址格式错误。请具体到区/县一级。").toString());
        }
    }

    @OnPrivate
    @Filter(value = "软件学院打卡信息解除绑定", matchType = MatchType.EQUALS)
    public void deleteTask(PrivateMsg msg, Sender sender) {
        String result = taskHealthService.updateTaskStatus(msg.getAccountInfo().getAccountCode(), "-1");
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡加入定时", matchType = MatchType.EQUALS)},
            customFilter = {"CodeFilter"}
    )
    public void createAutoTask(PrivateMsg msg, Sender sender) {
        String accountCode = msg.getAccountInfo().getAccountCode();
        String result = taskHealthService.creatAutoTask(accountCode, "");
        sender.sendPrivateMsg(accountCode, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }


    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡取消定时", matchType = MatchType.EQUALS)},
            customFilter = {"CodeFilter"}
    )
    public void deleteAutoTask(PrivateMsg msg, Sender sender) {
        String accountCode = msg.getAccountInfo().getAccountCode();
        String result = taskHealthService.deleteAutoTask(accountCode);
        sender.sendPrivateMsg(accountCode, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }

    @OnPrivate
    @Filter(value = "软件学院打卡任务查询", matchType = MatchType.EQUALS)
    public void searchTask(PrivateMsg msg, Sender sender) {
        TaskHealth taskHealth = taskHealthService.searchTask(msg.getAccountInfo().getAccountCode());
        sender.sendPrivateMsg(msg, SendMessageUtil.getTaskInfo(taskHealth));
    }

    @OnPrivate
    @Filter(value = "软件学院打卡任务日志查询", matchType = MatchType.EQUALS)
    public void searchTaskLog(PrivateMsg msg, Sender sender) {
        String accountCode = msg.getAccountInfo().getAccountCode();
        String s = taskHealthService.searchTaskLog(accountCode);
        if (!StrUtil.isBlank(s)) {
            sender.sendPrivateMsg(accountCode, SendMessageUtil.getHealthTaskMessageLog(s).toString());
        } else {
            sender.sendPrivateMsg(accountCode, SendMessageUtil.getHealthTaskMessageFailHeader().append("暂无打卡日志。").toString());
        }

    }

    @OnPrivate
    @Filter(value = "指令查询", matchType = MatchType.EQUALS)
    public void search(PrivateMsg msg, Sender sender) {
        sender.sendPrivateMsg(msg, SendMessageUtil.getOrder());
    }
    @OnPrivate
    @Filter(value = "2333", matchType = MatchType.EQUALS)
    public void search01(PrivateMsg msg, Sender sender) {
        sender.sendPrivateMsg(msg, SendMessageUtil.getOrder());
    }


}