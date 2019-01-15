package com.youguu.android.mq.listener;

import com.youguu.android.util.Constants;
import com.youguu.jms.message.ListenerType;
import com.youguu.jms.message.listener.impl.MessageBatchListenner;

/**
 * 安卓优顾理财监听
 * 支持单发和批量
 * @Title: YglcBatchListener.java
 * @Package com.youguu.android.mq.listener
 * @Description:
 * @author 徐云杰
 * @date 2015年7月30日 上午9:48:39
 * @version V1.0
 */
@ListenerType(destination=Constants.QUEUE_YGLC_BATCH)
public class YglcBatchListener extends MessageBatchListenner {

}
