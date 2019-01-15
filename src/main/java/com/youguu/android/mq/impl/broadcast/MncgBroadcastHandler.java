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
 * @ClassName: MncgBroadcastHandler
 * @Description: ios 消息处理
 * @date 2014年8月26日 下午2:48:22
 * {
 * 		"content":{
 * 			title:""
 * 			msg:""
 * 		}, android推送的格式  直接处理
 * 		"product":""
 * }
 */
@HandlerType(destination=Constants.QUEUE_MNCG_BROADCAST,type=1)
public class MncgBroadcastHandler extends MessageHandlerBasic {

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
		messageSendService.sendBroadcast(obj,Constants.PRODUCT_MNCG);
		log.debug("信鸽广播完成");

		//小米广播
		messageSendService = PushServerFactory.getServer(AndroidToken.PUSH_XIAOMI);
		messageSendService.sendBroadcast(obj,Constants.PRODUCT_MNCG);
		log.debug("小米广播完成");

		//华为广播
		messageSendService = PushServerFactory.getServer(AndroidToken.PUSH_HUAWEI);
		messageSendService.sendBroadcast(json,Constants.PRODUCT_MNCG);
		log.debug("华为广播完成");



	}

}
