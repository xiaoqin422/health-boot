package com.project.simbot.util;

import cn.hutool.core.io.resource.ResourceUtil;
import com.project.simbot.dao.NotifyDao;
import com.project.simbot.dao.RoleDao;
import com.project.simbot.dao.TaskHealthDao;
import com.project.simbot.dao.UserDao;
import love.forte.common.ioc.annotation.Beans;
import org.apache.ibatis.session.SqlSessionManager;

import java.io.InputStream;

/**
 * 包名: com.nuc.xiao.service
 * 类名: DBService
 * 创建用户: 25789
 * 创建日期: 2022年02月08日 0:43
 * 项目名: health
 *
 * @author: 秦笑笑
 **/
@Beans(init = true)
public class DBUtil {

    @Beans("sqlSessionManager")
    public SqlSessionManager getSqlSessionManager() {
        //加载核心配置文件
        InputStream resource = ResourceUtil.getStream("mybatis-config.xml");
        return SqlSessionManager.newInstance(resource);
    }

    @Beans(value = "taskHealthDao", init = true)
    public TaskHealthDao getTaskHealthDao(SqlSessionManager sqlSessionManager) {
        return sqlSessionManager.getMapper(TaskHealthDao.class);
    }

    @Beans(value = "roleDao", init = true)
    public RoleDao getRoleDao(SqlSessionManager sqlSessionManager) {
        return sqlSessionManager.getMapper(RoleDao.class);
    }

    @Beans(value = "notifyDao", init = true)
    public NotifyDao getNotifyDao(SqlSessionManager sqlSessionManager) {
        return sqlSessionManager.getMapper(NotifyDao.class);
    }
    @Beans(value = "userDao", init = true)
    public UserDao getUserDao(SqlSessionManager sqlSessionManager) {
        return sqlSessionManager.getMapper(UserDao.class);
    }

}