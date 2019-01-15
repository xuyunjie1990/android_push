package com.youguu.android.cache.impl;

import com.youguu.android.cache.ITokenLoadService;
import com.youguu.android.cache.UserTokenCache;
import com.youguu.android.util.RankTokenUser;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.push.pojo.AndroidToken;
import com.youguu.push.service.IPushUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by xyj on 2017/1/19.
 */
@Service("tokenLoadService")
public class TokenLoadServiceImpl implements ITokenLoadService {

    private Log log = LogFactory.getLog(ITokenLoadService.class);

    /**
     * 每次加载数量
     */
    private int batchSelectNum = 10000;

    @Resource
    private IPushUserService pushUserService;

    @Override
    public synchronized void incToken(int pushServer) {

        log.info("android token init start...");
        long stime = System.currentTimeMillis();

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH,-UserTokenCache.getDay());

        int count = 0;
        List<RankTokenUser> rankList  = new ArrayList<RankTokenUser>();
        RankTokenUser rankt;
        while(true){
            int maxId = UserTokenCache.getMaxId();

            //id 大于当前id不需要增量加载
            if(maxId>=UserTokenCache.getConf_maxId()){
                break;
            }
            List<AndroidToken> list = pushUserService.incLoadAndroidToken(pushServer,maxId, batchSelectNum);
            if(list!=null && list.size()>0){
                for(AndroidToken t:list){
                    //id 大于当前id不需要增量加载
                    if(t.getId() > UserTokenCache.getConf_maxId()){
                        break;
                    }

                    if(t.getLastDate()==null)
                    {
                        t.setLastDate(t.getCdate());
                    }

                    if(t.getLastDate().before(c.getTime()))
                    {
                        continue;
                    }

                    if(t.getToken() == null || "".equals(t.getToken())){
                        continue;
                    }

                    rankt = new RankTokenUser(t.getToken(),t.getApp(),t.getLastDate(),t.getId());
                    rankList.add(rankt);
                    maxId = t.getId();
                }
                count = count + list.size();
                log.info("android token load record:{}",count);
                UserTokenCache.setMaxId(maxId);
            }

            if(list==null || list.size()!=batchSelectNum){
                break;
            }
        }

        //排序
        Collections.sort(rankList);

        for(RankTokenUser t:rankList)
        {
            UserTokenCache.addToken(t.getApp(), t.getToken(),pushServer);
        }

        UserTokenCache.setInitOk();
        long end = System.currentTimeMillis();
        log.info("android token init end...count:{} times:{}",count,(end-stime));

    }
}
