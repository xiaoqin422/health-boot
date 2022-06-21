package com.project;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.project.simbot.dao.NotifyDao;
import com.project.simbot.util.DBUtil;
import org.apache.ibatis.session.SqlSessionManager;

/**
 * 包名: com.project
 * 类名: TestHttpClient
 * 创建用户: 25789
 * 创建日期: 2022年06月19日 15:33
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
public class TestHttpClient {
    private static DBUtil dbUtil;
    private static SqlSessionManager sqlSessionManager;
    public static void main(String[] args) {
        init();
        testNotifyDao();
    }
    public static void init(){
        dbUtil = new DBUtil();
        sqlSessionManager = dbUtil.getSqlSessionManager();

    }

    public static void testNotifyDao(){
        NotifyDao notifyDao = sqlSessionManager.getMapper(NotifyDao.class);
        // NotifyDao notifyDao = dbUtil.getNotifyDao(sqlSessionManager);
        // System.out.println(notifyDao.selectAll());
        System.out.println(notifyDao.searchStuCode("1913040637"));
        System.out.println(notifyDao.searchByGroupCode("19130406"));
    }

}