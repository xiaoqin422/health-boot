package com.project.simbot.dao;

import com.project.simbot.entity.TaskHealth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * (TaskHealth)表数据库访问层
 *
 * @author 秦笑笑
 * @since 2022-03-01 13:06:03
 */
public interface TaskHealthDao {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    TaskHealth selectById(Integer id);

    TaskHealth searchTaskByAccountCode(String accountCode);

    String searchTaskLogByAccountCode(String accountCode);

    int updateTaskStatus(@Param("accountCode") String accountCode, @Param("status") String status);

    int countOfPid(String pid);

    /**
     * 查询全部
     *
     * @return 对象列表
     */
    List<TaskHealth> selectAll();

    /**
     * 新增数据
     *
     * @param taskHealth 实例对象
     * @return 影响行数
     */
    int insert(TaskHealth taskHealth);


    /**
     * 修改数据
     *
     * @param taskHealth 实例对象
     * @return 影响行数
     */
    int update(TaskHealth taskHealth);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

