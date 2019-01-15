package com.youguu.android.mq.impl.factory;

import com.youguu.android.mq.impl.thread.BatchThread;
import com.youguu.android.util.SendUserInfo;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xyj on 2017/2/16.
 */
public class BroadcastThreadPoolFactory {

    private static Log log = LogFactory.getLog(BroadcastThreadPoolFactory.class);

    private static Map<Integer,ThreadPoolExecutor> cacheMap = new HashMap<>();

    /**
     *  核心线程数
     */
    private static int corePoolSize = 10;

    /**
     *  线程池中所允许创建最大线程数量
     */
    private static int maximumPoolSize = 20;

    /**
     *  当线程池中的线程数量大于核心线程数，
     如果这些多出的线程在经过了keepAliveTime时间后，
     依然处于空闲状态，那么这些多出的空闲线程将会被结束其生命周期。
     */
    private static long keepAliveTime = 30;

    /**
     *  keepAliveTime的时间单位
     */
    private static TimeUnit unit = TimeUnit.MINUTES;

    public static void putMessage(SendUserInfo suInfo,int pushServer)
    {
        try{
            ThreadPoolExecutor pool = cacheMap.get(pushServer);
            if(pool == null)
            {
                synchronized(BatchThreadPoolFactory.class)
                {
                    if(cacheMap.get(pushServer) == null)
                    {
                        pool = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,new ArrayBlockingQueue<Runnable>(10));
                        cacheMap.put(pushServer, pool);
                    }
                    pool = cacheMap.get(pushServer);
                }
            }
            pool.execute(new BatchThread(suInfo));
        }catch(Exception e){
            log.error("BroadcastThreadPoolFactory：" +suInfo.toString(),e);
        }
    }

}
