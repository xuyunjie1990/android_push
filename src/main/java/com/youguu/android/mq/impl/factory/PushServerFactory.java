package com.youguu.android.mq.impl.factory;

import com.youguu.android.service.ImessageSendService;
import com.youguu.android.service.impl.HuaWeiPushServiceImpl;
import com.youguu.android.service.impl.XiaoMiPushServiceImpl;
import com.youguu.android.service.impl.XinGePushServiceImpl;
import com.youguu.android.util.SpringUtil;
import com.youguu.push.pojo.AndroidToken;

/**
 * Created by xyj on 2017/1/17.
 * 推送对象工厂
 */
public class PushServerFactory {

    public static ImessageSendService getServer(int pushServer)
    {
        ImessageSendService messageSendService;
        switch (pushServer)
        {
            case AndroidToken.PUSH_XIAOMI:
                messageSendService = SpringUtil.getBean("xiaoMiPushService", XiaoMiPushServiceImpl.class);
                break;
            case AndroidToken.PUSH_HUAWEI:
                messageSendService =SpringUtil.getBean("huaWeiPushService",HuaWeiPushServiceImpl.class);
                break;
            default:
                messageSendService = SpringUtil.getBean("xinGePushService",XinGePushServiceImpl.class);
                break;
        }
        return messageSendService;
    }

}
