package com.project.simbot.service;

import com.project.simbot.entity.TaskHealth;

import java.util.List;

/**
 * 包名: com.project.simbot.service
 * 类名: TaskService
 * 创建用户: 25789
 * 创建日期: 2022年03月01日 1:27
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
public interface TaskHealthService {
    /**
     * 创建打卡记录
     *
     * @param accountCode QQ号
     * @param address     地址
     * @param pid         身份证号码
     * @return 创建结果
     */
    public String creatTask(String accountCode, String address, String pid);

    public String creatAutoTask(String accountCode, String groupCode);

    public String deleteAutoTask(String accountCode);

    /**
     * 执行一键打卡  1：执行完成  -1：任务不存在 0：任务异常
     *
     * @param accountCode 用户QQ号码
     * @return 执行结果
     */
    public int doTask(String accountCode);

    public String healthPost(String address, String pid);

    /**
     * 更改任务状态
     *
     * @param accountCode 用户QQ
     * @param status      状态值 -1删除 0禁用 1正常 2定时
     */
    public String updateTaskStatus(String accountCode, String status);

    /**
     * 通过QQ号查询打卡日志信息
     *
     * @param accountCode QQ号码
     * @return 打卡日志信息
     */
    public String searchTaskLog(String accountCode);

    /**
     * 更改打卡地址
     *
     * @param accountCode QQ号
     * @param address     地址
     * @return 更改结果
     */
    public String updateTaskAddress(String accountCode, String address);

    public void autoTask();

    /**
     * 查询任务状态
     *
     * @param accountCode QQ号
     * @return 查询结果
     */
    public TaskHealth searchTask(String accountCode);

    public List<TaskHealth> searchAllTask();

}
