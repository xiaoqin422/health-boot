package com.project.simbot.util;

import com.project.simbot.dao.RoleDao;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.bot.BotManager;

import java.util.List;

/**
 * 包名: com.project.simbot.util
 * 类名: ErrorUtil
 * 创建用户: 25789
 * 创建日期: 2022年06月19日 17:17
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans(init = true)
public class MsgUtil {
    private static final String ADMIN_CODE = "1";
    @Depend
    private  BotManager botManager;
    @Depend
    private  RoleDao roleDao;
    public void sendErrorMsg(String error){
        Sender sender = botManager.getDefaultBot().getSender().SENDER;
        List<String> adminCode = roleDao.selectAccountCodeByLevel(ADMIN_CODE);
        for (String code : adminCode) {
            sender.sendPrivateMsg(code,error);
        }
    }
    public void sendMsg(String msg){
        Sender sender = botManager.getDefaultBot().getSender().SENDER;
        List<String> adminCode = roleDao.selectAccountCodeByLevel(ADMIN_CODE);
        for (String code : adminCode) {
            sender.sendPrivateMsg(code,msg);
        }
    }
}