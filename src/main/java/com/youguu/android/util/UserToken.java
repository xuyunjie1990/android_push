package com.youguu.android.util;

import java.io.Serializable;

/**
 * Created by xyj on 2017/1/10.
 */
public class UserToken implements Serializable {

    private int userId;
    private String token;
    private int pushServer;

    public UserToken(){}

    public UserToken(int userId,String token)
    {
        this.userId = userId;
        this.token = token;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getPushServer() {
        return pushServer;
    }

    public void setPushServer(int pushServer) {
        this.pushServer = pushServer;
    }

    @Override
    public String toString() {
        return "UserToken{" +
                "userId=" + userId +
                ", token='" + token + '\'' +
                '}';
    }
}
