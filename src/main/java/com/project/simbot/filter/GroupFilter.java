package com.project.simbot.filter;

import com.project.simbot.dao.RoleDao;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.filter.FilterData;
import love.forte.simbot.filter.ListenerFilter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 包名: com.project.simbot.filter
 * 类名: GroupFilter
 * 创建用户: 25789
 * 创建日期: 2022年03月02日 21:24
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans("GroupFilter")
public class GroupFilter implements ListenerFilter {
    @Depend
    private RoleDao roleDao;

    @Override
    public boolean test(@NotNull FilterData data) {
        System.out.println(data.getMsgGet().getOriginalData());
        String originalData = data.getMsgGet().getOriginalData();
        List<String> list = roleDao.selectAllGroupCode();
        for (String s : list) {
            if (originalData.contains("group=" + s) || originalData.contains("groupId=" + s)) {
                return true;
            }
        }
        return false;
    }
}