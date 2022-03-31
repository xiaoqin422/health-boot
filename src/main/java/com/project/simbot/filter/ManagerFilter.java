package com.project.simbot.filter;

import com.project.simbot.dao.RoleDao;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.filter.FilterData;
import love.forte.simbot.filter.ListenerFilter;
import org.jetbrains.annotations.NotNull;

/**
 * 包名: com.project.simbot.filter
 * 类名: ManagerFilter
 * 创建用户: 25789
 * 创建日期: 2022年03月02日 23:12
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans("ManagerFilter")
public class ManagerFilter implements ListenerFilter {
    @Depend
    private RoleDao roleDao;

    @Override
    public boolean test(@NotNull FilterData data) {
        return roleDao.selectAccountCodeByLevel("1").contains(data.getMsgGet().getAccountInfo().getAccountCode());
    }
}