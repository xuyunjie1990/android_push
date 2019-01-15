package com.youguu.android.mq.impl.broadcast;

import com.alibaba.fastjson.JSONObject;
import com.youguu.android.mq.impl.factory.BroadcastThreadPoolFactory;
import com.youguu.android.util.SendUserInfo;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by xyj on 2017/1/19.
 */
@Service(value="disposeBroad")
public class DisposeBroad {

    private Log log = LogFactory.getLog(DisposeBroad.class);

    /**
     *  广播分配任务
     * @param tokenSet
     * @param pushServer
     * @param project
     */
    public  void allocatingTask(Set<String> tokenSet,int pushServer,String project,JSONObject json)
    {
        int count = tokenSet.size();
        int tradeNum = 0;
        List<String> tokens = new ArrayList<>(tokenSet);
        if(tokens.size()>0)
        {
            List<String> temp = new ArrayList<>();
            int num = 0;
            for(String token:tokens)
            {
                temp.add(token);
                num++;
                if(num == 10000)
                {
                    dis(temp,pushServer,project,json);
                    temp = new ArrayList<>();
                    num = 0;
                    tradeNum++;
                }
            }
            dis(temp,pushServer,project,json);
            tradeNum++;
            log.info("本次广播共计推送总数为：{}，共分{}个线程处理！",count,tradeNum);
        }
    }

    private void dis(List<String> tokens,int pushServer,String project,JSONObject json)
    {
        if(tokens.size() == 0)
        {
            return;
        }
        SendUserInfo suInfo = new SendUserInfo();
        //某推送服务下的全量token，这里不做批次拆分，在自身的服务下去实现拆分
        suInfo.setTokenList(tokens);
        suInfo.setJson(json);
        suInfo.setPushServer(pushServer);
        suInfo.setProduct(project);
        BroadcastThreadPoolFactory.putMessage(suInfo, pushServer);
    }

}
