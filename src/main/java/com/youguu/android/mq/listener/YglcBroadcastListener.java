package com.youguu.android.mq.listener;

import com.youguu.android.util.Constants;
import com.youguu.jms.message.ListenerType;
import com.youguu.jms.message.listener.impl.MessageListenerBasic;

/**
 * 安卓广播
 * @Title: YglcBroadcastListener.java
 * @Package com.youguu.android.mq.listener
 * @Description: 
 * @author 徐云杰
 * @date 2015年7月30日 下午5:26:39
 * @version V1.0
 */
@ListenerType(destination=Constants.QUEUE_YGLC_BROADCAST)
public class YglcBroadcastListener extends MessageListenerBasic{

}
