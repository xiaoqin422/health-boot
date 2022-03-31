package com.project.simbot.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.project.simbot.service.TaskWXTask;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;

/**
 * 包名: com.project.simbot.service.impl
 * 类名: TaskWXTaskServiceImpl
 * 创建用户: 25789
 * 创建日期: 2022年03月16日 16:49
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans
@Slf4j
public class TaskWXTaskServiceImpl implements TaskWXTask {
    private static final String keepLifeUrl = "http://zbjkttb.nuc.edu.cn/microapp/health_daily/userInfo";
    private static final String taskUrl = "http://zbjkttb.nuc.edu.cn/microapp/health_daily/report";
    @Override
    public boolean keepLife(String sessionID) {
        HttpRequest post = HttpUtil.createPost(keepLifeUrl);
        sessionID = "JSESSIONID=" + sessionID;
        post.cookie(sessionID);
        HttpResponse execute = post.execute();
        String body = execute.body();
        JSONObject jsonObject = JSON.parseObject(body);
        Integer status = (Integer)jsonObject.get("status");
        String msg = (String)jsonObject.get("msg");
        return status != 0 && !"未获取到用户信息，请关闭应用重新进入!".equals(msg);
    }

    @Override
    public boolean doTask(String session) {
        HttpRequest post = HttpUtil.createPost(taskUrl);
        session = "JSESSIONID=" + session;
        post.cookie(session);
        JSONObject param = JSON.parseObject("{\n" +
                "  \"address\": \"上阳村\",\n" +
                "  \"locationErrorExplain\": null,\n" +
                "  \"province\": \"山西省\",\n" +
                "  \"city\": \"运城市\",\n" +
                "  \"county\": \"芮城县\",\n" +
                "  \"distance\": 10,\n" +
                "  \"longitude\": 110.339155,\n" +
                "  \"latitude\": 34.725395,\n" +
                "  \"temperature\": \"35.8\",\n" +
                "  \"healthCondition\": \"正常\",\n" +
                "  \"healthConditionExplain\": null,\n" +
                "  \"familyCondition\": \"正常\",\n" +
                "  \"familyConditionExplain\": null,\n" +
                "  \"recentlyGoArea\": \"无\",\n" +
                "  \"recentlyGoAreaExplain\": null,\n" +
                "  \"ifContactCase\": \"无\",\n" +
                "  \"ifContactCaseExplain\": null,\n" +
                "  \"ifContactAreaBackPerson\": \"无\",\n" +
                "  \"ifContactAreaBackPersonExplain\": null,\n" +
                "  \"ifContactRjry\": \"无\",\n" +
                "  \"ifContactRjryExplain\": null,\n" +
                "  \"roomieTempIsUnusual\": \"否\",\n" +
                "  \"roomieTempUnusualDesc\": null,\n" +
                "  \"isConfirmed\": \"否\",\n" +
                "  \"confirmedDesc\": null,\n" +
                "  \"isRoommateToHotArea\": \"无\",\n" +
                "  \"roommateToHotAreaDesc\": null,\n" +
                "  \"isManyPeopleParty\": \"否\",\n" +
                "  \"manyPeoplePartyDesc\": null,\n" +
                "  \"ifReturnToSchool\": \"否\",\n" +
                "  \"ifReturnToSchoolExplain\": null,\n" +
                "  \"billingContactName\": \"秦老师\",\n" +
                "  \"billingContactNameTel\": \"17835766143\",\n" +
                "  \"specialSituation\": null,\n" +
                "  \"expectedDestination\": null,\n" +
                "  \"ifFromToFocusArea\": \"否\",\n" +
                "  \"ifFromToFocusAreaExplain\": \"\",\n" +
                "  \"fileUrl\": \",,,,,,\",\n" +
                "  \"time\": \"2022-03-15 13:32:00\",\n" +
                "  \"plusinfo\": \"Mozilla/5.0 (Linux; Android 12; Redmi K30 Pro Build/SKQ1.220201.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/95.0.4638.74 Mobile Safari/537.36 wxwork/4.0.0 ColorScheme/Light MicroMessenger/7.0.1 NetType/WIFI Language/zh Lang/zh\"\n" +
                "}");
        post.body(param.toString());
        HttpResponse execute = post.execute();
        String body = execute.body();
        System.out.println(body);
        JSONObject jsonObject = JSON.parseObject(body);
        Integer status = (Integer)jsonObject.get("status");
        String msg = (String)jsonObject.get("msg");
        return status == 1 && "保存成功!".equals(msg);
    }
}