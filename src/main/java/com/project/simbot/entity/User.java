package com.project.simbot.entity;

import java.io.Serializable;

/**
 * (User)实体类
 *
 * @author 秦笑笑
 * @since 2022-06-19 22:39:47
 */
public class User implements Serializable {
    private static final long serialVersionUID = 296067892540426523L;

    private Integer id;

    private String username;

    private String password;

    private String token;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "User {" +
                "id : " + id + ", " +
                "username : " + username + ", " +
                "password : " + password + ", " +
                "token : " + token + ", " +
                '}';
    }
}

