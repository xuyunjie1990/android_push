package com.youguu.android.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youguu.android.cache.ITokenLoadService;
import com.youguu.android.cache.UserTokenCache;
import com.youguu.android.mq.impl.broadcast.DisposeBroad;
import com.youguu.android.service.ImessageSendService;
import com.youguu.android.util.Constants;
import com.youguu.android.util.ProductConfig;
import com.youguu.android.util.SendUserInfo;
import com.youguu.android.util.UserToken;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.core.pojo.Response;
import com.youguu.push.pojo.AndroidToken;
import org.springframework.stereotype.Component;
import plugins.huawei.Hpush;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by xyj on 2017/1/6.
 */
@Component("huaWeiPushService")
public class HuaWeiPushServiceImpl implements ImessageSendService {

    private Log log = LogFactory.getLog(ImessageSendService.class);

    @Resource
    private DisposeBroad disposeBroad;

    @Resource
    private ITokenLoadService tokenLoadService;

    public int singleSend(String token, String project,String json) {
        String message = getMessage(json,project);
        return Hpush.getCacheHpush().single_send_check(token, project, message);
    }

    public int batchSend(List<String> tokens, String project, String json) {
        String message = getMessage(json,project);
        int result = 0;
        for(int i=0;i<tokens.size();i=i+1000)
        {
            int end = i+1000;
            if(end > tokens.size()){
                end = tokens.size();
            }
            result = Hpush.getCacheHpush().batch_send(tokens.subList(i, end),project,message);
            if(result != 0){
                break;
            }
        }
        return result;
    }

    /**
     * 处理消息
     * @Title: getMessage
     * @Description:
     * @param json
     * @return
     * Message    返回类型
     */
    private String getMessage(String json,String product)
    {
        JSONObject obj = JSONObject.parseObject(json);
        JSONObject toJson = obj.getJSONObject(Constants.FIELD_CONTENT);
        ProductConfig config = ProductConfig.getProductConfig(String.format(Constants.HUAWEI,product));
        toJson.put(Constants.FIELD_DEPLOY_STATUS, config.getDeployStatus());
        return toJson.toString();
    }

    @Override
    public Response<List<String>> sendBatch(SendUserInfo suInfo) {
        com.alibaba.fastjson.JSONObject obj = suInfo.getJson();
        Response<List<String>> res = new Response<>();
        String product = suInfo.getProduct();
        List<String> errTokens = new ArrayList<>();
        res.setCode("0001");
        res.setT(errTokens);
        List<String> tokens;
        List<UserToken> userTokenList;
        int result = 0;
        String token = "";
        int uid;
        Map<Integer,String> tokenMap;

        try {
            com.youguu.jms.pojo.Message<com.alibaba.fastjson.JSONObject> msgData = new com.youguu.jms.pojo.Message<>();
            msgData.parse(obj.toJSONString(), com.alibaba.fastjson.JSONObject.class);
            com.alibaba.fastjson.JSONObject json = msgData.getMsg();

            tokens = suInfo.getTokenList();

            //标示是否需要统一发送
            int isBatch =  json.getIntValue(Constants.FIELD_BATCH);
            if(isBatch == Constants.BATCH_YES)
            {
                result = batchSend(tokens,product,json.toJSONString());
                if(result == Constants.GUANGBO_STATUS_NO)
                {
                    log.info("huawei batchSend  result:{}，tokens={}","失败",tokens);
                    for(String t :tokens)
                    {
                        errTokens.add(t);
                    }
                }
            }
            else
            {
                JSONArray uids = json.getJSONArray(Constants.FIELD_TOUIDS);
                userTokenList = suInfo.getUtList();
                tokenMap = getTokenMap(userTokenList);
                for(int i=0;i<uids.size();i++)
                {
                    uid = uids.getIntValue(i);
                    com.alibaba.fastjson.JSONObject msg = json.getJSONObject(Constants.FIELD_CONTENT);
                    if(msg.containsKey("custom_content")){
                        msg.getJSONObject("custom_content").put(Constants.FIELD_RUID, uid);
                    }
                    token = tokenMap.get(uid);
                    result = singleSend(token, product,json.toJSONString());
                    if(result == Constants.GUANGBO_STATUS_NO)
                    {
                        log.info("huawei singleSend  result:{}，token={}","失败",token);
                        errTokens.add(token);
                    }
                }
            }
            res.setCode("0000");
        } catch (Exception e) {
            log.error("huawei send error",e);
            errTokens.add(token);
        }
        return res;
    }

    private Map<Integer,String> getTokenMap(List<UserToken> userTokenList)
    {
        Map<Integer,String> tokenMap = new HashMap<>();
        for(UserToken ut:userTokenList)
        {
            tokenMap.put(ut.getUserId(),ut.getToken());
        }
        return tokenMap;
    }

    @Override
    public void sendBroadcast(com.alibaba.fastjson.JSONObject msg, String product) {
        //设置为批量群发
        msg.getJSONObject("msg").put(Constants.FIELD_BATCH,Constants.BATCH_YES);
        //增量拉取一下新的token
        tokenLoadService.incToken(AndroidToken.PUSH_HUAWEI);
        //获取所有对应产品下对应服务的token
        Set<String> tokensSet = UserTokenCache.getProductTokens(product, AndroidToken.PUSH_HUAWEI);
        //开始广播
        disposeBroad.allocatingTask(tokensSet,AndroidToken.PUSH_HUAWEI,product,msg);
    }
}
