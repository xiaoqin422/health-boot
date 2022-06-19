package com.project.simbot.dao;

import com.project.simbot.entity.Role;

import java.util.List;

/**
 * (Role)表数据库访问层
 *
 * @author 秦笑笑
 * @since 2022-03-02 21:06:43
 */
public interface RoleDao {

    /**
     * 通过QQ号码查询单条数据
     *
     * @param accountCode QQ号码
     * @return 实例对象
     */
    Role selectByAccountCode(String accountCode);

    /**
     * 通过QQ群号查询单条数据
     *
     * @param groupCode QQ群号
     * @return 实例对象
     */
    Role selectByGroupCode(String groupCode);

    /**
     * 根据账户等级查找用户
     *
     * @param level 账户等级
     * @return 查询结果集
     */
    List<String> selectAccountCodeByLevel(String level);
    List<String> selectGroupCodeByLevel(String level);
    /**
     * 查找所有的权限群组
     *
     * @return 查询结果集
     */
    List<String> selectAllGroupCode();

    /**
     * 查找所有的权限账户
     *
     * @return 查询结果集
     */
    List<String> selectAllAccountCode();

    /**
     * 查询全部
     *
     * @return 对象列表
     */
    List<Role> selectAll();


    /**
     * 新增数据
     *
     * @param role 实例对象
     * @return 影响行数
     */
    int insert(Role role);


    /**
     * 修改数据
     *
     * @param role 实例对象
     * @return 影响行数
     */
    int update(Role role);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}

