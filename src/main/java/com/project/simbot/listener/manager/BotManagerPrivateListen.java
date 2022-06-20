package com.project.simbot.listener.manager;

import com.project.simbot.dao.RoleDao;
import com.project.simbot.entity.Role;
import com.project.simbot.entity.TaskHealth;
import com.project.simbot.service.TaskHealthService;
import com.project.simbot.service.TaskNotifyService;
import com.project.simbot.util.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.FilterValue;
import love.forte.simbot.annotation.Filters;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.filter.MatchType;

import java.util.List;

/**
 * 包名: com.project.simbot.listener
 * 类名: BotManagerPrivateListen
 * 创建用户: 25789
 * 创建日期: 2022年03月02日 22:53
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans
@Slf4j
public class BotManagerPrivateListen {
    @Depend
    private TaskHealthService taskHealthService;
    @Depend
    private RoleDao roleDao;
    @Depend
    private TaskNotifyService taskNotifyService;

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务禁用-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void managerTaskOFF(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        String result = taskHealthService.updateTaskStatus(accountCode, "0");
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务启用-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void managerTaskON(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        String result = taskHealthService.updateTaskStatus(accountCode, "1");
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务删除-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void deleteTask(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        String result = taskHealthService.updateTaskStatus(accountCode, "-1");
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务加入定时-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void addAutoTask(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        String result = taskHealthService.creatAutoTask(accountCode, "");
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务取消定时-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void deleteAutoTask(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        String result = taskHealthService.deleteAutoTask(accountCode);
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append(result).toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡执行定时", matchType = MatchType.EQUALS)},
            customFilter = {"ManagerFilter"}
    )
    public void doAutoTask(PrivateMsg msg, Sender sender) {
        taskHealthService.autoTask();
        sender.sendPrivateMsgAsync(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务查询-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void searchTask(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        TaskHealth taskHealth = taskHealthService.searchTask(msg.getAccountInfo().getAccountCode());
        sender.sendPrivateMsg(msg, SendMessageUtil.getTaskInfo(taskHealth));
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务管理", matchType = MatchType.EQUALS)},
            customFilter = {"ManagerFilter"}
    )
    public void searchAllTask(PrivateMsg msg, Sender sender) {
        List<TaskHealth> taskHealths = taskHealthService.searchAllTask();
        StringBuilder resultMsg = new StringBuilder("查询结果。");
        for (int i = 0; i < taskHealths.size(); i++) {
            TaskHealth taskHealth = taskHealths.get(i);
            if (taskHealth.getStatus().equals("-1")) continue;
            resultMsg.append("\n-------------------------------------\n\t任务【" + (i + 1) + "】\n");
            resultMsg.append(SendMessageUtil.getTaskInfoNotHeader(taskHealth));
        }
        sender.sendPrivateMsg(msg, resultMsg.toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡添加用户-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void addAccountCodeToAutoTask(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        Role role = roleDao.selectByAccountCode(accountCode);
        if (role == null) {
            role = new Role();
            role.setAccountCode(accountCode);
            role.setLevel("0");
            role.setType("0");
            roleDao.insert(role);
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("用户添加成功").toString());
        } else {
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageFailHeader().append("用户已经存在").toString());
        }
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡设置管理员-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void addManagerAccountCode(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        Role role = roleDao.selectByAccountCode(accountCode);
        if (role == null) {
            role = new Role();
            role.setAccountCode(accountCode);
            role.setLevel("1");
            role.setType("0");
            roleDao.insert(role);
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("管理员添加成功").toString());
        } else if (role.getLevel().equals("0")) {
            role.setLevel("1");
            roleDao.insert(role);
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("管理员添加成功").toString());
        } else if (role.getLevel().equals("1")) {
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageFailHeader().append("管理员已经存在").toString());
        }
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡删除用户-{{accountCode,[1-9]([0-9]{4,10})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void deleteManagerAccountCode(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        Role role = roleDao.selectByAccountCode(accountCode);
        if (role == null) {
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageFailHeader().append("用户不存在").toString());
        } else if (role.getLevel().equals("1")) {
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageFailHeader().append("用户删除失败！").append("\n 该用户为管理员").toString());
        } else {
            roleDao.deleteById(role.getId());
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("用户删除成功").toString());
        }
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡用户管理", matchType = MatchType.EQUALS)},
            customFilter = {"ManagerFilter"}
    )
    public void selectAllAccountCode(PrivateMsg msg, Sender sender) {
        List<String> accountList = roleDao.selectAccountCodeByLevel("0");
        StringBuilder resultMsg = new StringBuilder("用户列表。").append("\n-------------------------------------\n");
        if (accountList.isEmpty()) {
            sender.sendPrivateMsg(msg, resultMsg.append("暂无用户。").toString());
            return;
        }
        for (int i = 0; i < accountList.size(); i++) {
            resultMsg.append("【").append(i + 1).append("】\t").append(accountList.get(i)).append("\n");
        }
        sender.sendPrivateMsg(msg, resultMsg.toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡添加群聊-{{groupCode,[1-9]([0-9]{4,9})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void addGroupCode(PrivateMsg msg, Sender sender, @FilterValue("groupCode") String groupCode) {
        Role group = roleDao.selectByGroupCode(groupCode);
        if (group == null) {
            group = new Role();
            group.setAccountCode(groupCode);
            group.setLevel("0");
            group.setType("1");
            roleDao.insert(group);
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("群聊添加成功").toString());
        } else {
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageFailHeader().append("群聊已经存在").toString());
        }
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡群聊监听-{{groupCode,[1-9]([0-9]{4,9})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void setGroupNotify(PrivateMsg msg, Sender sender, @FilterValue("groupCode") String groupCode) {
        Role group = roleDao.selectByGroupCode(groupCode);
        if (group == null) {
            group = new Role();
            group.setAccountCode(groupCode);
            group.setLevel("1");
            group.setType("1");
            roleDao.insert(group);
        } else if ("0".equals(group.getLevel())) {
            group.setLevel("1");
            roleDao.update(group);
        }
        sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("监听群聊成功").toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡群聊监听取消-{{groupCode,[1-9]([0-9]{4,9})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void setGroupNotifyFalse(PrivateMsg msg, Sender sender, @FilterValue("groupCode") String groupCode) {
        Role group = roleDao.selectByGroupCode(groupCode);
        if (group != null && "1".equals(group.getLevel())) {
            group.setLevel("0");
            roleDao.update(group);
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("取消群聊监听成功").toString());
        } else {
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("请先添加群聊").toString());
        }
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡删除群聊-{{groupCode,[1-9]([0-9]{4,9})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void deleteGroupCode(PrivateMsg msg, Sender sender, @FilterValue("groupCode") String groupCode) {
        Role group = roleDao.selectByGroupCode(groupCode);
        if (group == null) {
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageFailHeader().append("群聊不存在").toString());
        } else {
            roleDao.deleteById(group.getId());
            sender.sendPrivateMsg(msg, SendMessageUtil.getHealthTaskMessageSuccessHeader().append("群聊删除成功").toString());
        }
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡群聊管理", matchType = MatchType.EQUALS)},
            customFilter = {"ManagerFilter"}
    )
    public void searchAllGroup(PrivateMsg msg, Sender sender) {
        List<String> groupList = roleDao.selectAllGroupCode();
        StringBuilder resultMsg = new StringBuilder("群聊列表。").append("\n-------------------------------------\n");
        if (groupList.isEmpty()) {
            sender.sendPrivateMsg(msg, resultMsg.append("暂无群聊。").toString());
            return;
        }
        for (int i = 0; i < groupList.size(); i++) {
            resultMsg.append("【").append(i + 1).append("】\t").append(groupList.get(i)).append("\n");
        }
        sender.sendPrivateMsg(msg, resultMsg.toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务日志管理", matchType = MatchType.EQUALS)},
            customFilter = {"ManagerFilter"}
    )
    public void searchAllLog(PrivateMsg msg, Sender sender) {
        List<TaskHealth> taskHealthList = taskHealthService.searchAllTask();
        StringBuilder resultMsg = new StringBuilder("打卡日志。");
        for (int i = 0; i < taskHealthList.size(); i++) {
            TaskHealth taskHealth = taskHealthList.get(i);
            resultMsg.append("\n-------------------------------------\n\t任务【").append(i + 1).append("】\n")
                    .append("绑定账户：").append(taskHealth.getAccountCode()).append("\n")
                    .append(SendMessageUtil.getHealthTaskMessageLogNoHeader(taskHealth.getLog()));
        }
        sender.sendPrivateMsg(msg, resultMsg.toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡任务日志查询-{{accountCode,[1-9]([0-9]{4,9})}}", matchType = MatchType.REGEX_MATCHES)},
            customFilter = {"ManagerFilter"}
    )
    public void searchLog(PrivateMsg msg, Sender sender, @FilterValue("accountCode") String accountCode) {
        String s = taskHealthService.searchTaskLog(accountCode);
        StringBuilder resultMsg = new StringBuilder("打卡日志。").append("\n-------------------------------------\n")
                .append("绑定账户：").append(accountCode).append("\n");
        if (!"".equals(s)) {
            sender.sendPrivateMsg(accountCode, resultMsg.append(SendMessageUtil.getHealthTaskMessageLogNoHeader(s)).toString());
        } else {
            sender.sendPrivateMsg(accountCode, resultMsg.append("暂无打卡日志。").toString());
        }
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡查询", matchType = MatchType.EQUALS)},
            customFilter = {"ManagerFilter"}
    )
    public void getNotifyTask(PrivateMsg msg, Sender sender) {
        sender.sendPrivateMsg(msg,taskNotifyService.getUnFinishMsg("454062801",false,false).toString());
    }

    @OnPrivate
    @Filters(value = {
            @Filter(value = "软件学院打卡督促", matchType = MatchType.EQUALS)},
            customFilter = {"ManagerFilter"}
    )
    public void doNotifyTask(PrivateMsg msg, Sender sender) {
        sender.sendPrivateMsg(msg,taskNotifyService.getUnFinishMsg("454062801",false,true).toString());
    }
}