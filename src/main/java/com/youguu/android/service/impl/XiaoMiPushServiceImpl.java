package com.youguu.android.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youguu.android.service.ImessageSendService;
import com.youguu.android.util.*;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.core.pojo.Response;
import org.springframework.stereotype.Component;
import plugins.xiaomi.XmPush;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xyj on 2017/1/9.
 */
@Component("xiaoMiPushService")
public class XiaoMiPushServiceImpl implements ImessageSendService {

    private Log log = LogFactory.getLog(ImessageSendService.class);

    public int singleSend(String token, String project, String json) {
        XmPush push = createSend(project,json);
        return push.single_send(token);
    }

    public int batchSend(List<String> tokens, String project, String json) {
        XmPush push = createSend(project,json);
        int result = 0;
        for(int i=0;i<tokens.size();i=i+1000)
        {
            int end = i+1000;
            if(end > tokens.size()){
                end = tokens.size();
            }
            result = push.batch_send(tokens.subList(i, end));
            if(result != 0){
                break;
            }
        }
        return result;
    }

    public int allSend(String project, String json) {
        XmPush push = createSend(project,json);
        return push.push_all_send();
    }

    /**
     * 构造push对象
     * @param project
     * @param json
     * @return
     */
    private XmPush createSend(String project, String json)
    {
        ProductConfig config = ProductConfig.getProductConfig(String.format(Constants.XIAOMI,project));
        String appSecret = config.getSecretKey();
        String packageName = config.getPackageName();
        String message = getMessage(json,config);
        XmPush push = new XmPush(appSecret,packageName,project.equals("001") ? "优顾炒股":"优顾理财");
        push.createMessage(message);
        return push;
    }

    /**
     * 处理消息
     * @Title: getMessage
     * @Description:
     * @param json
     * @return
     * Message    返回类型
     */
    private String getMessage(String json,ProductConfig config)
    {
        JSONObject obj =JSONObject.parseObject(json);
        JSONObject toJson = obj.getJSONObject(Constants.FIELD_CONTENT);
        toJson.put(Constants.FIELD_DEPLOY_STATUS, config.getDeployStatus());
        return toJson.toString();
    }

    @Override
    public Response<List<String>> sendBatch(SendUserInfo suInfo)
    {
        com.alibaba.fastjson.JSONObject obj = suInfo.getJson();
        Response<List<String>> res = new Response<>();
        String product = suInfo.getProduct();
        List<String> errTokens = new ArrayList<>();
        res.setCode("0001");
        res.setT(errTokens);
        List<String> tokens;
        List<UserToken> userTokenList;
        int result = 0;
        String token="";
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
                    log.info("xiaomi batchSend  result:{}，tokens={}","失败",tokens);
                    //TODO 小米在高频率的单发下，很容易收到小米返回的推送失败状态，所以暂不做删除token
//                    for(String t :tokens)
//                    {
//                        errTokens.add(t);
//                    }
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
                    JSONObject msg = json.getJSONObject(Constants.FIELD_CONTENT);
                    if(msg.containsKey("custom_content")){
                        msg.getJSONObject("custom_content").put(Constants.FIELD_RUID, uid);
                    }
                    token = tokenMap.get(uid);
                    result = singleSend(token, product,json.toJSONString());
                    if(result == Constants.GUANGBO_STATUS_NO)
                    {
                        log.info("xiaomi singleSend  result:{}，token={}","失败",token);
                        //errTokens.add(token);
                    }
                }
            }
            res.setCode("0000");
        } catch (Exception e) {
            log.error("xiaomi send android error",e);
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
    public void sendBroadcast(JSONObject msg, String product) {
        try {
            int result =allSend(product,msg.toJSONString());

            if(result != 0)
            {
                flag:for(int i=0;i<3;i++)
                {
                    result = allSend(product,msg.toJSONString());
                    if(result == 0)
                    {
                        break flag;
                    }
                }
                if(result != 0)
                {
                    log.error("xiaomi sendBroadcast  errorCode:{}",result);
                    log.error("xiaomi sendBroadcast  xiaomiErrorMsg:{}", PushUtil.getErrorMsgByCode(result));
                }
                else
                {
                    log.info("小米广播发送成功");
                }
            }
            else
            {
                log.info("小米广播发送成功");
            }
        } catch (Exception e) {
            log.error("xiaoMi sendBroadcast error",e);
        }
    }
}
