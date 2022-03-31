package com.project.simbot.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * (Role)实体类
 *
 * @author 秦笑笑
 * @since 2022-03-02 21:06:42
 */
@Data
public class Role implements Serializable {
    private static final long serialVersionUID = 477413295635485663L;

    private Integer id;
    /**
     * 权限账号
     */
    private String accountCode;
    /**
     * 权限等级 1超级管理员
     */
    private String level;
    /**
     * 类型 0账户 1为群组
     */
    private String type;

}

