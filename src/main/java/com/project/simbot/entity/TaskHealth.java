package com.project.simbot.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * (TaskHealth)实体类
 *
 * @author 秦笑笑
 * @since 2022-03-01 01:23:45
 */
@Data
public class TaskHealth implements Serializable {
    private static final long serialVersionUID = 154826478771750113L;

    private Integer id;

    /**
     * QQ号码
     */
    private String accountCode;

    private String groupCode;
    /**
     * 身份证
     */
    private String pid;
    /**
     * 地址
     */
    private String address;

    /**
     * 用户状态  -1为删除  1为正常  0位黑名单
     */
    private String status;
    /**
     * 日志
     */
    private String log;

}

