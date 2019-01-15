package com.youguu.android.mq.impl.thread;

import com.youguu.android.mq.impl.factory.PushServerFactory;
import com.youguu.android.service.ImessageSendService;
import com.youguu.android.util.SendUserInfo;
import com.youguu.android.util.SpringUtil;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.core.pojo.Response;
import com.youguu.push.service.IPushUserService;

import java.util.List;

/**
 * Created by xyj on 2017/1/17.
 */
public class BatchThread implements Runnable  {

    private Log log = LogFactory.getLog(BatchThread.class);

    /**
     * 消息结构体
     */
    private SendUserInfo suInfo;

    public BatchThread(SendUserInfo suInfo) {
        this.suInfo = suInfo;
    }

    @Override
    public void run() {
        if(this.suInfo != null)
        {
            log.info("线程{}开始批量处理",Thread.currentThread().getName());
            long sendTimes = System.currentTimeMillis();
            ImessageSendService messageSendService = PushServerFactory.getServer(this.suInfo.getPushServer());
            Response<List<String>> res = messageSendService.sendBatch(this.suInfo);
            if(res!=null && "0000".equals(res.getCode())){
                List<String> errorList = res.getT();
                if(errorList.size()>0){
                    IPushUserService ps = SpringUtil.getBean(IPushUserService.class);
                    ps.delXinGeTokens(errorList);
                }
                log.info("线程{}批量发送完成，耗时:{}ms， 失败{}",Thread.currentThread().getName(),(System.currentTimeMillis()-sendTimes),errorList.size());
            }
            else
            {
                log.info("发送异常，token:{}",res.getT());
            }
        }
    }

}
