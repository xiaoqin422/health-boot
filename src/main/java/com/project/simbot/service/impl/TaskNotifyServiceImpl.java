package com.project.simbot.service.impl;

import catcode.CatCodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.project.simbot.dao.NotifyDao;
import com.project.simbot.dao.RoleDao;
import com.project.simbot.dao.UserDao;
import com.project.simbot.entity.Notify;
import com.project.simbot.entity.User;
import com.project.simbot.service.TaskNotifyService;
import com.project.simbot.util.MsgUtil;
import com.project.simbot.util.SendMessageUtil;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.api.message.results.GroupMemberInfo;
import love.forte.simbot.api.message.results.GroupMemberList;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.bot.BotManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 包名: com.project.simbot.service.impl
 * 类名: TaskNotifyServiceImpl
 * 创建用户: 25789
 * 创建日期: 2022年06月19日 17:06
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans
public class TaskNotifyServiceImpl implements TaskNotifyService {
    @Depend
    private NotifyDao notifyDao;
    @Depend
    private BotManager botManager;
    @Depend
    private UserDao userDao;
    @Depend
    private MsgUtil msgUtil;
    @Depend
    private RoleDao roleDao;
    private static final String CLASS_URL = "http://yx.ty-ke.com/Teacher/Tiwenxinxi/tiwenxinxi_data";
    private static final String STU_URL = "http://yx.ty-ke.com/Teacher/Tiwenxinxi/tiwenxinxi_list";
    private static final String LOGIN_URL = "http://yx.ty-ke.com//Teacher/Login/login_data";

    @Override
    public boolean classIsFinished() {
        String percentage = getClassFinishMsg();
        if (!StrUtil.isEmptyIfStr(percentage) && percentage.startsWith("100")) {
            return true;
        } else {
            Map<String, Object> stuMsg = getStuMsg();
            return stuMsg.size() == 0;
        }
    }

    @Override
    public Map<String, Object> getUnFinishMsg() {
        return getStuMsg();
    }

    @Override
    public String getClassFinishMsg() {
        HttpRequest client = HttpUtil.createPost(CLASS_URL);
        client.header("authorization", loginAndGetToken());
        Map param = new HashMap<>();
        param.put("teacher_id", 313);
        param.put("role_id", 3);
        client.form(param);
        HttpResponse response = client.execute();
        JSONObject jsonObject = JSON.parseObject(response.body());
        if (200 == Integer.parseInt(jsonObject.get("code").toString())) {
            List<JSONObject> list = (List<JSONObject>) jsonObject.get("data");
            list = list.stream().filter(object -> "19130406".equals(object.get("title").toString()))
                    .collect(Collectors.toList());
            JSONObject object = list.get(0);
            Object percentage = object.get("percentage");
            Object student_num = object.get("student_num");
            Object student_wan_num = object.get("student_wan_num");
            Object shiduan1 = object.get("shiduan1");
            Object shiduan2 = object.get("shiduan2");
            Object shiduan3 = object.get("shiduan3");
            return percentage.toString() + "  打卡数：" + student_wan_num.toString() + "/"
                    + student_num.toString() + "  打卡情况：" + shiduan1.toString() + "/" +
                    shiduan2.toString() + "/" + shiduan3.toString();
        } else {
            msgUtil.sendErrorMsg((String) jsonObject.get("msg"));
        }
        return "班级查询接口错误。";
    }

    @Override
    public void doAtNotify(boolean isNotifySelf) {
        Getter getter = botManager.getDefaultBot().getSender().GETTER;
        Sender sender = botManager.getDefaultBot().getSender().SENDER;
        if (!classIsFinished()) {
            List<String> groupCode = roleDao.selectGroupCodeByLevel("1");
            for (String s : groupCode) {
                // 获取群来中的永辉西南西
                GroupMemberList groupMemberList = getter.getGroupMemberList(s);
                // 获取需要监控的班级成员QQ列表
                List<String> notifies = notifyDao.searchByGroupCode(s);
                // 获取到群中所有是监听群组的成员
                groupMemberList.stream().filter(obj -> notifies.contains(obj.getAccountCode())).collect(Collectors.toList());
                // 获取没有完成任务的人员
                Map<String, Object> unFinishMsg = getStuMsg();
                StringBuilder msg = SendMessageUtil.generateNotifyMsg();
                StringBuilder nameList = SendMessageUtil.generateHeadFace().append("未打卡人员:");
                StringBuilder atList = SendMessageUtil.generateHeadFace().append("\"打卡不规范，开学两行泪\"");
                // 群聊中被监听的班级成员
                for (GroupMemberInfo info : groupMemberList) {
                    String accountCode = info.getAccountCode();
                    Notify notify = notifyDao.searchStuCode(accountCode);
                    // 判断该用户是否完成打卡
                    if (notify != null && unFinishMsg.get(notify.getStuNumber()) != null) {
                        nameList.append(notify.getStuName() + " ");
                        atList.append(CatCodeUtil.INSTANCE.getNekoTemplate().at(accountCode));
                        if (isNotifySelf){
                            sender.sendPrivateMsgAsync(accountCode,s,SendMessageUtil.generateNotifyMsg().append("及时完成打卡哟~").toString());
                        }
                    }
                }
                msg.append(nameList).append("\n").append(atList);
                sender.sendGroupMsg(getter.getGroupInfo(s), msg + "\n及时完成打卡哟~");
            }
        }

    }

    private Map<String, Object> getStuMsg() {
        HttpRequest client = HttpUtil.createPost(STU_URL);
        Map param = new HashMap<>();
        param.put("teacher_id", 313);
        param.put("role_id", 4);
        param.put("cha_id", 919);
        client.form(param);
        HttpResponse response = client.execute();
        JSONObject jsonObject = JSON.parseObject(response.body());
        Map<String, Object> res = new HashMap<>();
        if (200 == (Integer) jsonObject.get("code")) {
            List<JSONObject> list = (List<JSONObject>) jsonObject.get("data");
            list = list.stream().filter(object -> !isFinish(object)).collect(Collectors.toList());
            for (JSONObject object : list) {
                String stu_number = (String) object.get("stu_number");
                res.put(stu_number, object);
            }
        } else {
            msgUtil.sendErrorMsg((String) jsonObject.get("msg"));
        }
        return res;
    }

    private boolean isFinish(JSONObject jsonObject) {
        Integer zao = (Integer) jsonObject.get("zao");
        Integer zhong = (Integer) jsonObject.get("zhong");
        Integer wan = (Integer) jsonObject.get("wan");
        return 1 == zao || 1 == zhong || 1 == wan;
    }

    private String loginAndGetToken() {
        User user = userDao.selectById(1);
        HttpRequest request = HttpUtil.createPost(LOGIN_URL);
        Map param = new HashMap();
        param.put("username", user.getUsername());
        param.put("password", user.getPassword());
        request.form(param);
        HttpResponse response = request.execute();
        JSONObject jsonObject = JSON.parseObject(response.body());
        String res = "";
        if (200 == Integer.parseInt((String) jsonObject.get("code"))) {
            JSONObject data = (JSONObject) jsonObject.get("res");
            res = data.get("token").toString();
            user.setToken(res);
            userDao.update(user);
        } else {
            msgUtil.sendMsg("初始化Token失败。error" + jsonObject.get("msg"));
        }
        return res;
    }
}