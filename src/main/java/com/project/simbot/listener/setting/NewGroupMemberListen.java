package com.project.simbot.listener.setting;

import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.*;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilder;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.containers.AccountInfo;
import love.forte.simbot.api.message.containers.BotInfo;
import love.forte.simbot.api.message.containers.GroupAccountInfo;
import love.forte.simbot.api.message.containers.GroupInfo;
import love.forte.simbot.api.message.events.FriendAddRequest;
import love.forte.simbot.api.message.events.GroupAddRequest;
import love.forte.simbot.api.message.events.GroupMemberIncrease;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.filter.MatchType;
import love.forte.simbot.listener.ContinuousSessionScopeContext;
import love.forte.simbot.listener.ListenerContext;
import love.forte.simbot.listener.SessionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这是一个 自动通过加群申请并自动迎新 的实例监听器。
 *
 * @author ForteScarlet
 */
@Beans
@Slf4j
public class NewGroupMemberListen {
    private static final String Group_GROUP_ADD = "group_add";
    /**
     * 注入得到一个消息构建器工厂。
     */
    @Depend
    private MessageContentBuilderFactory messageBuilderFactory;

    @Depend
    private BotManager botManager;
    /**
     * 用来缓存入群申请的时候所填的信息。
     */
    private static final Map<String, String> REQUEST_TEXT_MAP = new ConcurrentHashMap<>();

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NewGroupMemberListen.class);

    /**
     * {@link OnGroupAddRequest} 是一个模板注解，其等同于 {@code @Listen(GroupAddRequest.class)},
     * 即监听一个 {@link GroupAddRequest} 类型的事件。
     * <p>
     * {@link GroupAddRequest} 顾名思义，即 “群添加申请” 事件。
     * <p>
     * 这个事件不仅仅代表别人加入某群，也有可能代表有人邀请当前bot入群。
     * <p>
     * 当然了，如果是处理其他人的加群申请，那么这个bot必须是个管理员才能接收到请求事件。
     *
     * @param groupAddRequest 群添加申请/邀请事件。
     * @param setter          一般用来通过申请，使用的是Setter。当然，你也可以使用 {@link love.forte.simbot.api.sender.MsgSender#SETTER}, 它们所代表的是同一个对象。
     * @see GroupAddRequest
     */
    @OnGroupAddRequest
    @Filters(customFilter = "GroupFilter")
    public void onRequest(GroupAddRequest groupAddRequest, ListenerContext context, Getter getter, Setter setter) {
        String groupCode = groupAddRequest.getGroupInfo().getGroupCode();
        // 获取私聊持续上下文环境
        ContinuousSessionScopeContext sessionContext =
                (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        // 此事件的“申请者”
        AccountInfo accountInfo = groupAddRequest.getRequestAccountInfo();
        // 收到此事件的bot
        BotInfo botInfo = groupAddRequest.getBotInfo();

        // 如果上述两者的账号不相同，则说明此事件不是bot被邀请，而是别人申请入群。
        // 这步判断操作似乎很繁琐，未来版本可能会提供更简洁的方案
        if (!accountInfo.getAccountCode().equals(botInfo.getBotCode())) {
            // 获取入群的时候的申请消息（如果有的话
            String text = groupAddRequest.getText();
            if (text != null) {
                REQUEST_TEXT_MAP.put(accountInfo.getAccountCode(), text);
            }
            GroupInfo groupInfo = groupAddRequest.getGroupInfo();
            Sender sender = botManager.getDefaultBot().getSender().SENDER;
            LOGGER.info("{}({}) 申请加入群 {}({}), 申请备注：{}",
                    accountInfo.getAccountNickname(), accountInfo.getAccountCode(),
                    groupInfo.getGroupName(), groupInfo.getGroupCode(),
                    text
            );

            sender.sendGroupMsg(groupInfo, accountInfo.getAccountNickname() + "("
                    + accountInfo.getAccountCode() + ")" + "申请加入群(" + groupCode + ")，申请备注：(" + text + ")\n"
                    + "处理申请指令【同意/拒绝入群-QQ号码】");
            assert sessionContext != null;
            sessionContext.waiting(Group_GROUP_ADD, accountInfo.getAccountCode(), 0, SessionCallback.<String>builder().onResume(Result -> {
                setter.setGroupAddRequest(groupAddRequest.getFlag(), Result.equals("是"), false, null);
            }).build());
        }

    }

    @OnGroup
    @OnlySession(group = Group_GROUP_ADD)
    @Filters(value = {
            @Filter(value = "同意入群-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)}
            , customFilter = "GroupFilter")
    public void newGroupRequestAccept(GroupMsg msg, Sender sender, ListenerContext context, @FilterValue("accountCode") String accountCode) {
        // 获取私聊持续上下文环境
        ContinuousSessionScopeContext sessionContext =
                (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        String code = msg.getAccountInfo().getAccountCode();
        REQUEST_TEXT_MAP.put(accountCode + "-同意申请", code);
        log.info(code + "同意" + accountCode + "的入群申请");
        sender.sendGroupMsgAsync(msg, code + "同意" + accountCode + "的入群申请");
        assert sessionContext != null;
        sessionContext.push(Group_GROUP_ADD, accountCode, "是");
    }

    @OnGroup
    @OnlySession(group = Group_GROUP_ADD)
    @Filters(value = {
            @Filter(value = "拒绝入群-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)}
            , customFilter = "GroupFilter")
    public void newGroupRequestNoAccept(GroupMsg msg, Sender sender, ListenerContext context, @FilterValue("accountCode") String accountCode) {
        GroupAccountInfo code = msg.getAccountInfo();
        // 获取私聊持续上下文环境
        ContinuousSessionScopeContext sessionContext =
                (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        log.info(code + "拒绝" + accountCode + "的入群申请");
        sender.sendGroupMsgAsync(msg, code + "拒绝" + accountCode + "的入群申请");
        assert sessionContext != null;
        sessionContext.push(Group_GROUP_ADD, accountCode, "否");
    }

    @OnFriendAddRequest
    public void newFriendAdd(FriendAddRequest friendAddRequest, ListenerContext context, Sender sender, Setter setter) {
        AccountInfo accountInfo = friendAddRequest.getAccountInfo();
        String text = friendAddRequest.getText();
        // 获取私聊持续上下文环境
        ContinuousSessionScopeContext sessionContext =
                (ContinuousSessionScopeContext) context.getContext(ListenerContext.Scope.CONTINUOUS_SESSION);
        LOGGER.info("{}({}) 申请添加好友, 申请备注：{}",
                accountInfo.getAccountNickname(), accountInfo.getAccountCode(),
                text
        );
        sender.sendPrivateMsg("2578908933", accountInfo.getAccountNickname() + "("
                + accountInfo.getAccountCode() + ")" + "申请添加好友,申请备注：" + text);
        setter.setFriendAddRequest(friendAddRequest.getFlag(), text, true, false);
    }

    /**
     * 新人入群申请之后，便是 ”群成员增加“ 事件，如果你想要什么迎新操作，建议都在这个事件中处理。
     * <p>
     * 通过 {@link OnGroupMemberIncrease} 监听群人数增加事件，这也是一个模板注解，其等效于 {@code @Listen(GroupMemberIncrease.class)}
     *
     * @param groupMemberIncrease 群人数增加事件实例
     * @param sender              既然是”迎新“示例，则当然要发消息。
     * @see GroupMemberIncrease
     */
    @OnGroupMemberIncrease
    public void newGroupMember(GroupMemberIncrease groupMemberIncrease, Sender sender) {
        // 得到一个消息构建器。
        MessageContentBuilder builder = messageBuilderFactory.getMessageContentBuilder();

        // 入群者信息
        AccountInfo accountInfo = groupMemberIncrease.getAccountInfo();

        // 尝试从缓存中获取他入群的时候所记录的信息
        // 如果不希望看到null，则记得自行处理。
        String text = REQUEST_TEXT_MAP.remove(accountInfo.getAccountCode());
        String accept = REQUEST_TEXT_MAP.remove(accountInfo.getAccountCode() + "-同意申请");
        // 假设我们的迎新消息是这样的：
        /*
            @xxx 欢迎入群！
            你的入群申请信息是：xxxxxx
         */
        MessageContent msg = builder
                // at当事人
                .at(accountInfo)
                // tips 通过 \n 换行
                .text(" 欢迎入群！\n")
                .text("你的入群申请信息是：").text(text).text("\n")
                .text("审批人：").at(accept)
                .build();

        // 增加了人的群信息
        GroupInfo groupInfo = groupMemberIncrease.getGroupInfo();

        // 发送消息
        sender.sendGroupMsg(groupInfo, msg);
    }


    @OnGroup
    public void groupSetting(GroupMsg msg, Setter setter) {
        // setter.setMsgRecall()
    }
}
