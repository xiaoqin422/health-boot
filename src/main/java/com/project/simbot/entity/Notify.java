package com.project.simbot.entity;

import java.io.Serializable;

/**
 * (Notify)实体类
 *
 * @author 秦笑笑
 * @since 2022-06-19 17:02:07
 */
public class Notify implements Serializable {
    private static final long serialVersionUID = -61801937571718794L;
    
    private Integer id;
    /**
    * 学号
    */
    private String stuNumber;
    /**
    * 姓名
    */
    private String stuName;
    /**
    * QQ号
    */
    private String stuCode;
    /**
    * 群聊账号
    */
    private String groupCode;

        
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
        
    public String getStuNumber() {
        return stuNumber;
    }

    public void setStuNumber(String stuNumber) {
        this.stuNumber = stuNumber;
    }
        
    public String getStuName() {
        return stuName;
    }

    public void setStuName(String stuName) {
        this.stuName = stuName;
    }
        
    public String getStuCode() {
        return stuCode;
    }

    public void setStuCode(String stuCode) {
        this.stuCode = stuCode;
    }
        
    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    @Override
    public String toString(){
        return "Notify {" +
            "id : " + id + ", " +
            "stuNumber : " + stuNumber + ", " +
            "stuName : " + stuName + ", " +
            "stuCode : " + stuCode + ", " +
            "groupCode : " + groupCode + ", " +
        '}';
    }
}

