package com.youguu.android.util;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xyj on 2017/1/11.
 */
public class SendUserInfo  implements Serializable {

    private JSONObject json;
    private List<UserToken> utList;
    private List<String> tokenList;
    private int pushServer;
    private String product;

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }

    public List<UserToken> getUtList() {
        return utList;
    }

    public void setUtList(List<UserToken> utList) {
        this.utList = utList;
    }

    public List<String> getTokenList() {
        return tokenList;
    }

    public void setTokenList(List<String> tokenList) {
        this.tokenList = tokenList;
    }

    public int getPushServer() {
        return pushServer;
    }

    public void setPushServer(int pushServer) {
        this.pushServer = pushServer;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "SendUserInfo{" +
                "pushServer=" + pushServer +
                ", json=" + json +
                '}';
    }
}
