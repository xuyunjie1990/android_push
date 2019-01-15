package com.youguu.android.mq.impl.broadcast;

import com.alibaba.fastjson.JSONObject;
import com.youguu.android.mq.impl.MessageHandlerBasic;
import com.youguu.android.mq.impl.factory.PushServerFactory;
import com.youguu.android.service.ImessageSendService;
import com.youguu.android.util.Constants;
import com.youguu.jms.message.handler.HandlerType;
import com.youguu.jms.pojo.Message;
import com.youguu.push.pojo.AndroidToken;

/**
 * 
 * @Title: YglcBroadcastHandler.java
 * @Package com.youguu.android.mq.impl.broadcast
 * @Description: 
 * {
 * 		"content":{
 * 			title:""
 * 			msg:""
 * 		}, android推送的格式  直接处理
 * 		"product":""
 * }
 * @author 徐云杰
 * @date 2015年8月5日 上午10:49:19
 * @version V1.0
 */
@HandlerType(destination=Constants.QUEUE_YGLC_BROADCAST,type=1)
public class YglcBroadcastHandler extends MessageHandlerBasic {

	@Override
	public void handler(JSONObject json) {
		log.debug("receive: "+json.toJSONString());
		Message<JSONObject> msg = new Message<>();
		msg.parse(json.toJSONString(), JSONObject.class);
		JSONObject  obj = msg.getMsg();
		//这里需要对所有推送服务都进行广播调用
		ImessageSendService  messageSendService;

		//信鸽广播
		messageSendService = PushServerFactory.getServer(AndroidToken.PUSH_XINGE);
		messageSendService.sendBroadcast(obj,Constants.PRODUCT_YGLC);
		log.debug("信鸽广播完成");

		//小米广播
		messageSendService = PushServerFactory.getServer(AndroidToken.PUSH_XIAOMI);
		messageSendService.sendBroadcast(obj,Constants.PRODUCT_YGLC);
		log.debug("小米广播完成");

		//华为广播
		messageSendService = PushServerFactory.getServer(AndroidToken.PUSH_HUAWEI);
		messageSendService.sendBroadcast(json,Constants.PRODUCT_YGLC);
		log.debug("华为广播完成");
	}

}
