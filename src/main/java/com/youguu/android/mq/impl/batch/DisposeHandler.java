package com.youguu.android.mq.impl.batch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youguu.android.mq.impl.factory.BatchThreadPoolFactory;
import com.youguu.android.util.Constants;
import com.youguu.android.util.SendUserInfo;
import com.youguu.android.util.UserToken;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.push.pojo.AndroidToken;
import com.youguu.push.service.IPushUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by xyj on 2017/1/17.
 */
@Service(value="disposeHandler")
public class DisposeHandler {

    private Log log = LogFactory.getLog(DisposeHandler.class);

    @Resource
    IPushUserService pushUserService;

    /**
     *  获取所有用户信息
     * @param set
     * @return
     */
    public Map<Integer,UserToken> getAllUserToken(Set<Integer> set,String product)
    {
        Map<Integer,UserToken> map = new HashMap<>();
        if(set != null)
        {
            List<Integer> userIds = new ArrayList<>();
            Iterator<Integer> it = set.iterator();
            List<AndroidToken> listToken;
            UserToken ut;
            while(it.hasNext())
            {
                userIds.add(it.next());
                if(userIds.size() == 1000)
                {
                    listToken = pushUserService.findTokenList(userIds,product);
                    if(listToken != null)
                    {
                        for(AndroidToken xinge:listToken)
                        {
                            ut = new UserToken();
                            ut.setUserId(xinge.getUid());
                            ut.setToken(xinge.getToken());
                            ut.setPushServer(xinge.getPushServer());
                            map.put(xinge.getUid(),ut);
                        }
                    }
                    userIds.clear();
                }
            }
            listToken = pushUserService.findTokenList(userIds,product);
            if(listToken != null)
            {
                for(AndroidToken xinge:listToken)
                {
                    ut = new UserToken();
                    ut.setUserId(xinge.getUid());
                    ut.setToken(xinge.getToken());
                    ut.setPushServer(xinge.getPushServer());
                    map.put(xinge.getUid(),ut);
                }
            }
        }
        return map;
    }

    public  Set<Integer> getAllUserId(JSONArray array)
    {
        JSONObject jsonObject;
        Set<Integer> set = new HashSet<>();
        for(int i =0; i< array.size(); i ++)
        {
            jsonObject = array.getJSONObject(i);
            com.youguu.jms.pojo.Message<JSONObject> ygMsg = new com.youguu.jms.pojo.Message<>();
            ygMsg.parse(jsonObject.toJSONString(),JSONObject.class);
            JSONObject data = ygMsg.getMsg();
            JSONArray uids = data.getJSONArray(Constants.FIELD_TOUIDS);
            List<Integer> userIds = JSONArray.parseArray(uids.toJSONString(),Integer.class);
            for(int uid:userIds)
            {
                set.add(uid);
            }
        }
        return set;
    }

    /**
     *  解析json，返回用户ID集合
     * @param json
     * @return
     */
    public  List<Integer> parseJson(JSONObject json)
    {
        com.youguu.jms.pojo.Message<JSONObject> ygMsg = new com.youguu.jms.pojo.Message<>();
        ygMsg.parse(json.toJSONString(),JSONObject.class);
        JSONObject data = ygMsg.getMsg();
        JSONArray uids = data.getJSONArray(Constants.FIELD_TOUIDS);
        if(uids == null)
        {
            return null;
        }
        List<Integer> userIds = JSONArray.parseArray(uids.toJSONString(),Integer.class);
        return userIds;
    }

    /**
     *  对用户token进行分服务处理
     * @param userIds
     * @return
     */
    public  Map<Integer,List<String>> getTokenMap(List<Integer> userIds,Map<Integer,List<UserToken>> utMap,Map<Integer,UserToken> allInfo)
    {
        UserToken xinge;
        Map<Integer,List<String>> map = new HashMap<>();
        UserToken	ut;
        List<String> list;
        List<UserToken> listUt;
        for(int uid:userIds)
        {
            xinge = allInfo.get(uid);
            if(xinge != null)
            {
                if(map.get(xinge.getPushServer()) == null)
                {
                    list = new ArrayList<>();
                    list.add(xinge.getToken());
                    map.put(xinge.getPushServer(),list);
                }
                else
                {
                    map.get(xinge.getPushServer()).add(xinge.getToken());
                }

                if(utMap.get(xinge.getPushServer()) == null)
                {
                    listUt = new ArrayList<>();
                    ut = new UserToken(xinge.getUserId(),xinge.getToken());
                    listUt.add(ut);
                    utMap.put(xinge.getPushServer(),listUt);
                }
                else
                {
                    utMap.get(xinge.getPushServer()).add(new UserToken(xinge.getUserId(),xinge.getToken()));
                }
            }
        }
        return map;
    }

    /**
     *  分配任务
     * @param tokenMap
     * @param json
     */
    public  void allocatingTask(Map<Integer,List<String>> tokenMap,JSONObject json,Map<Integer,List<UserToken>> utMap,String project)
    {
        if(tokenMap == null || tokenMap.size()==0)
        {
            return;
        }
        SendUserInfo suInfo;
        for(Integer pushServer:tokenMap.keySet())
        {
            suInfo = new SendUserInfo();
            suInfo.setTokenList(tokenMap.get(pushServer));
            suInfo.setUtList(utMap.get(pushServer));
            suInfo.setJson(json);
            suInfo.setPushServer(pushServer);
            suInfo.setProduct(project);
            BatchThreadPoolFactory.putMessage(suInfo,pushServer);
        }
    }

}
