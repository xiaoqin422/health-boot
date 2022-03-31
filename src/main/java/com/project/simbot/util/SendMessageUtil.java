package com.project.simbot.util;

import com.project.simbot.entity.TaskHealth;

/**
 * 包名: com.project.simbot.util
 * 类名: SendMessageUtil
 * 创建用户: 25789
 * 创建日期: 2022年03月03日 17:26
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
public class SendMessageUtil {
    public static StringBuilder getHealthTaskMessageSuccessHeader() {
        return new StringBuilder().append("指令执行完毕。"
                + "\n--------------------------------------\n");
    }

    public static StringBuilder getHealthTaskMessageFailHeader() {
        return new StringBuilder().append("指令执行异常。"
                + "\n--------------------------------------\n");
    }

    public static StringBuilder getHealthTaskMessageLog(String log) {
        String[] split = log.split("\\|");
        return getHealthTaskMessageSuccessHeader().append(
                "居民身份证：" + split[0] +
                        "\n打卡时间：" + split[1] +
                        "\n体温：" + split[2] +
                        "\n反馈信息：" + split[3]);
    }

    public static String getHealthTaskMessageLogNoHeader(String log) {
        String[] split = log.split("\\|");
        return "居民身份证：" + split[0] +
                "\n打卡时间：" + split[1] +
                "\n体温：" + split[2] +
                "\n反馈信息：" + split[3];
    }

    public static String getOrder() {
        return "私聊指令\n" +
                "--------------------------------------\n" +
                "【1】软件学院打卡\n" +
                "【2】软件学院打卡-身份证号-打卡地址\n" +
                "【3】软件学院打卡信息绑定\n" +
                "【4】软件学院打卡修改打卡地址-新打卡地址\n" +
                "【5】软件学院打卡信息解除绑定\n" +
                "【6】软件学院打卡任务查询\n" +
                "【7】软件学院打卡加入定时（需管理员添加用户）\n" +
                "【8】软件学院打卡取消定时（需管理员添加用户）\n" +
                "【9】软件学院打卡任务日志查询\n" +
                "【10】指令查询\n" +
                "--------------------------------------\n" +
                "群聊指令\n" +
                "--------------------------------------\n" +
                "【1】软件学院打卡\n" +
                "【2】软件学院打卡加入定时\n" +
                "【3】软件学院打卡取消定时\n" +
                "【4】软件学院打卡任务查询\n" +
                "【5】软件学院打卡任务日志查询\n" +
                "【6】指令查询\n" +
                "--------------------------------------\n" +
                "管理指令（需管理员权限）\n" +
                "--------------------------------------\n" +
                "【1】  软件学院打卡任务禁用-QQ号码\n" +
                "【2】  软件学院打卡任务启用-QQ号码\n" +
                "【3】  软件学院打卡任务删除-QQ号码\n" +
                "【4】  软件学院打卡任务加入定时-QQ号码\n" +
                "【5】  软件学院打卡任务取消定时-QQ号码\n" +
                "【6】  软件学院打卡任务查询-QQ号码\n" +
                "【7】  软件学院打卡任务管理\n" +
                "【8】  软件学院打卡添加用户-QQ号码\n" +
                "【9】  软件学院打卡删除用户-QQ号码\n" +
                "【10】软件学院打卡用户管理\n" +
                "【11】软件学院打卡设置管理员-QQ号码\n" +
                "【12】软件学院打卡添加群聊-QQ群号码\n" +
                "【13】软件学院打卡删除群聊-QQ群号码\n" +
                "【14】软件学院打卡群聊管理\n" +
                "【15】软件学院打卡任务日志管理\n" +
                "【16】软件学院打卡任务日志查询-QQ号码\n" +
                "【17】软件学院打卡执行定时\n";
    }

    public static String getTaskInfo(TaskHealth taskHealth) {
        StringBuilder msg = new StringBuilder("查询结果。"
                + "\n-------------------------------------\n");
        if (taskHealth == null || taskHealth.getStatus().equals("-1")) {
            msg.append("暂无打卡任务");
        } else {
            String pid = taskHealth.getPid();
            pid = pid.replace(pid.substring(6, 14), "****");
            msg.append("绑定账户：").append(taskHealth.getAccountCode()).append("\n")
                    .append("居民身份证：").append(pid).append("\n")
                    .append("打卡地址：").append(taskHealth.getAddress()).append("\n")
                    .append("任务状态：").append(!"0".equals(taskHealth.getStatus()) ? "启用" : "禁用").append("\n")
                    .append("是否加入定时：").append("2".equals(taskHealth.getStatus()) ? "是" : "否").append("\n");
        }
        return msg.toString();
    }

    public static StringBuilder getTaskInfoNotHeader(TaskHealth taskHealth) {
        String pid = taskHealth.getPid();
        pid = pid.replace(pid.substring(6, 14), "****");
        return new StringBuilder().append("绑定账户：").append(taskHealth.getAccountCode()).append("\n")
                .append("居民身份证：").append(pid).append("\n")
                .append("打卡地址：").append(taskHealth.getAddress()).append("\n")
                .append("任务状态：").append(!"0".equals(taskHealth.getStatus()) ? "启用" : "禁用").append("\n")
                .append("是否加入定时：").append("2".equals(taskHealth.getStatus()) ? "是" : "否").append("\n");
    }
}