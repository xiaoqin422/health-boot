package com.project.simbot.listener.group;

import catcode.CatCodeUtil;
import cn.hutool.core.util.ReUtil;
import com.project.simbot.entity.TaskHealth;
import com.project.simbot.service.TaskHealthService;
import com.project.simbot.util.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.Filters;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.filter.MatchType;


/**
 * 群消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Beans} 注解。
 *
 * @author ForteScarlet
 */
@Beans
@Slf4j
public class GroupListen {
    @Depend
    private TaskHealthService taskHealthService;

    /**
     * 此监听函数代表，收到消息的时候，将消息的各种信息打印出来。
     * <p>
     * 此处使用的是模板注解 {@link OnGroup}, 其代表监听一个群消息。
     * <p>
     * 由于你监听的是一个群消息，因此你可以通过 {@link GroupMsg} 作为参数来接收群消息内容。
     *
     * <p>
     * 注意！ 假如你发现你群消息发不出去（或者只有一些很短的消息能发出去）且没有任何报错，
     * 但是尝试后，发现 <b>私聊</b> 一切正常，能够发送，那么这是 <b>正常现象</b>！
     * <p>
     * 参考：
     */
    @OnGroup
    @Filters(value = {
            @Filter(value = "软件学院打卡", matchType = MatchType.EQUALS)},
            customFilter = {"GroupFilter"}
    )
    public void onGroupMsg(GroupMsg groupMsg, Sender sender) {
        String accountCode = groupMsg.getAccountInfo().getAccountCode();
        String groupCode = groupMsg.getGroupInfo().getGroupCode();
        sender.sendGroupMsg(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode) + "开始执行指令任务...");
        int code = taskHealthService.doTask(accountCode);
        if (code == -1) {
            sender.sendGroupMsgAsync(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                    + SendMessageUtil.getHealthTaskMessageFailHeader().append("请先私聊进行信息绑定，格式：【软件学院打卡信息绑定】").toString());
        } else if (code == 0) {
            sender.sendGroupMsgAsync(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                    + SendMessageUtil.getHealthTaskMessageFailHeader().append("任务已被禁用,请联系管理员！").toString());
        } else {
            String log = taskHealthService.searchTaskLog(accountCode);
            sender.sendGroupMsgAsync(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                    + SendMessageUtil.getHealthTaskMessageLog(log).toString());
        }
    }

    @OnGroup
    @Filters(value = {
            @Filter(value = "软件学院打卡任务日志查询", matchType = MatchType.EQUALS)},
            customFilter = {"GroupFilter"}
    )
    public void searchTaskLog(GroupMsg groupMsg, Sender sender) {
        String accountCode = groupMsg.getAccountInfo().getAccountCode();
        String s = taskHealthService.searchTaskLog(accountCode);
        sender.sendGroupMsg(groupMsg.getGroupInfo().getGroupCode(), CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode) + "开始执行指令任务...");
        if (!"".equals(s)) {
            sender.sendGroupMsgAsync(groupMsg, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                    + SendMessageUtil.getHealthTaskMessageLog(s).toString());
        } else {
            sender.sendGroupMsgAsync(groupMsg, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                    + SendMessageUtil.getHealthTaskMessageFailHeader().append("暂无打卡日志。").toString());
        }
    }

    @OnGroup
    @Filters(value = {
            @Filter(value = "软件学院打卡任务查询", matchType = MatchType.EQUALS)},
            customFilter = {"GroupFilter"}
    )
    public void searchTask(GroupMsg groupMsg, Sender sender) {
        String accountCode = groupMsg.getAccountInfo().getAccountCode();
        sender.sendGroupMsg(groupMsg.getGroupInfo().getGroupCode(), CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode) + "开始执行指令任务...");
        TaskHealth taskHealth = taskHealthService.searchTask(accountCode);
        sender.sendGroupMsgAsync(groupMsg, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                + SendMessageUtil.getTaskInfo(taskHealth));
    }

    @OnGroup
    @Filters(value = {
            @Filter(value = "软件学院打卡加入定时", matchType = MatchType.EQUALS)},
            customFilter = {"GroupFilter"}
    )
    public void createAutoTask(GroupMsg groupMsg, Sender sender) {
        String accountCode = groupMsg.getAccountInfo().getAccountCode();
        String groupCode = groupMsg.getGroupInfo().getGroupCode();
        sender.sendGroupMsg(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode) + "开始执行指令任务...");
        String result = taskHealthService.creatAutoTask(accountCode, groupCode);
        sender.sendGroupMsgAsync(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                + SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }


    @OnGroup
    @Filters(value = {
            @Filter(value = "软件学院打卡取消定时", matchType = MatchType.EQUALS)},
            customFilter = {"GroupFilter"}
    )
    public void deleteAutoTask(GroupMsg groupMsg, Sender sender) {
        String accountCode = groupMsg.getAccountInfo().getAccountCode();
        String groupCode = groupMsg.getGroupInfo().getGroupCode();
        sender.sendGroupMsg(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode) + "开始执行指令任务...");
        String result = taskHealthService.deleteAutoTask(accountCode);
        sender.sendGroupMsgAsync(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                + SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }


    @OnGroup
    @Filters(value = {
            @Filter(value = "指令查询", matchType = MatchType.EQUALS)},
            customFilter = {"GroupFilter"})
    public void search(GroupMsg msg, Sender sender) {
        String accountCode = msg.getAccountInfo().getAccountCode();
        sender.sendGroupMsgAsync(msg.getGroupInfo(), CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode)
                + SendMessageUtil.getHealthTaskMessageSuccessHeader().append(SendMessageUtil.getOrder()).toString());
    }

    @OnGroup
    @Filters(value = {
            @Filter(value = "软件学院打卡-{{taskMsg,\\d{6}(18|19|20)\\d{2}(0\\d|10|11|12)([0-2]\\d|30|31)\\d{3}[\\dXx]-.+(省|市).+(市|区).+(区|县).*$}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"GroupFilter"}
    )
    public void health(GroupMsg msg, Sender sender, @FilterValue("taskMsg") String taskMsg) {
        String groupCode = msg.getGroupInfo().getGroupCode();
        String accountCode = msg.getAccountInfo().getAccountCode();
        sender.sendGroupMsg(groupCode, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode) + "开始执行指令任务...");
        final String regex = "\\d{6}(18|19|20)\\d{2}(0\\d|10|11|12)([0-2]\\d|30|31)\\d{3}[\\dXx]";
        String pid = ReUtil.get(regex, taskMsg, 0);
        String address = taskMsg.replaceAll(regex, "");
        String s = taskHealthService.healthPost(address, pid);
        sender.sendGroupMsgAsync(msg, CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode) +
                SendMessageUtil.getHealthTaskMessageLog(s).toString());
    }
}
