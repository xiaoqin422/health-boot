package com.project.simbot.listener.privat;

import cn.hutool.core.util.ReUtil;
import com.mchange.v2.resourcepool.TimeoutException;
import com.project.simbot.service.TaskHealthService;
import com.project.simbot.util.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.annotation.OnlySession;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.ContinuousSessionScopeContext;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.SessionCallback;

/**
 * 私聊消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Beans} 注解。
 *
 * @author ForteScarlet
 */
@Beans
@Slf4j
public class TaskHealthPrivateListen {
    private static final String GROUP_PID = "Health_PID";
    private static final String GROUP_ADDRESS = "Health_ADDRESS";
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
    @Filter(value = "软件学院打卡", matchType = MatchType.EQUALS)
    public void replyPrivateMsg1(PrivateMsg privateMsg, ListenerContext context, Sender sender) {
        // 获取私聊持续上下文环境
        ContinuousSessionScopeContext sessionContext =
                (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        // 处理sessionContext为null
        assert sessionContext != null;
        String accountCode = privateMsg.getAccountInfo().getAccountCode();
        int result = taskHealthService.doTask(accountCode);
        if (result == 1 || result == 2) {
            String s = taskHealthService.searchTaskLog(accountCode);
            sender.sendPrivateMsg(privateMsg, SendMessageUtil.getHealthTaskMessageLog(s).toString());
            return;
        } else if (result == 0) {
            sender.sendPrivateMsg(privateMsg, SendMessageUtil.getHealthTaskMessageFailHeader().append("任务已被禁用,请联系管理员！").toString());
            return;
        }
        sender.sendPrivateMsg(privateMsg, SendMessageUtil.getHealthTaskMessageFailHeader().append("账户信息未绑定，请根据提示完成账户绑定。\n居民身份证号码").toString());
        final SessionCallback<String> callback = SessionCallback.<String>builder().onResume(pid -> {
            // 得到手机号，进行下一步操作
            sender.sendPrivateMsg(privateMsg, "签到地址【具体到区/县一级】");

            // 这是在回调中继续创建一个会话。
            // 这里通过 sessionContext.waiting(group, key, OnResume) 快速创建一个回调，只处理正确结果的情况，而不处理其他（出现错误、关闭事件等）
            // wait, 这里使用的是 name_group，也就是等待提供姓名的group，但是key还是那个人对应唯一的key
            sessionContext.waiting(GROUP_ADDRESS, accountCode, SessionCallback.<String>builder().onResume(address -> {
                // address，结合上一个会话的 pid 发出结果消息
                String resultMsg = taskHealthService.creatTask(accountCode, address, pid);
                sender.sendPrivateMsg(privateMsg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(resultMsg).toString());
            }).onErrorAndCancel(e -> {
                sender.sendPrivateMsg(privateMsg, SendMessageUtil.getHealthTaskMessageFailHeader().append("签到地址异常:").append(e).toString());
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
    @Filter(value = "软件学院打卡-{{taskMsg,\\d{6}(18|19|20)\\d{2}(0\\d|10|11|12)([0-2]\\d|30|31)\\d{3}[\\dXx]-.+(省|市).+(市|区).+(区|县).*$}}", matchType = MatchType.REGEX_MATCHES)
    public void health(PrivateMsg msg, Sender sender, @FilterValue("taskMsg") String taskMsg) {
        final String regex = "\\d{6}(18|19|20)\\d{2}(0\\d|10|11|12)([0-2]\\d|30|31)\\d{3}[\\dXx]";
        String pid = ReUtil.get(regex, taskMsg, 0);
        String address = taskMsg.replaceAll(regex, "");
        String s = taskHealthService.healthPost(address, pid);
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageLog(s).toString());
    }

}
