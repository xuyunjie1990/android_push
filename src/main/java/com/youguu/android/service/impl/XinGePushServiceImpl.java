package com.youguu.android.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.youguu.android.service.ImessageSendService;
import com.youguu.android.util.*;
import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.core.pojo.Response;
import org.springframework.stereotype.Component;
import plugins.xinge.json.JSONObject;
import plugins.xinge.tencent.Message;
import plugins.xinge.tencent.XingeApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 信鸽推送实现类
 * @Title: XinGePushServiceImpl.java
 * @Package com.youguu.android.service.impl
 * @Description:
 * @author 徐云杰
 * @date 2015年7月30日 上午11:05:14
 * @version V1.0
 */
@Component("xinGePushService")
public class XinGePushServiceImpl implements ImessageSendService {

	private XinGePushServiceImpl xinGePushService;

	private Log log = LogFactory.getLog(ImessageSendService.class);

	public int singleSend(String deviceToken,String json,String product) {
		XingeApp xg = getXingeApp(product);
		Message msg = getMessage(json,product);
		return xg.pushSingleDevice(deviceToken,msg).getInt(Constants.XINGE_RESULT_CODE);
	}

	public int batchSend(List<String> deviceList,String json, String product) {
		int result = 0;
		XingeApp xg = getXingeApp(product);
		Message msg = getMessage(json,product);
		String pushId = createMultipush(product,msg);

		//信鸽推送单次上限1000
		for(int i=0;i<deviceList.size();i=i+1000){
			int end = i+1000;
			if(end > deviceList.size()){
				end = deviceList.size();
			}
			JSONObject object = xg.pushDeviceListMultiple(pushId, deviceList.subList(i, end));
			result = object.getInt(Constants.XINGE_RESULT_CODE);
			//TODO 暂时先这样处理
			if(result != 0){
				break;
			}
		}
		return result;
	}

	public int allSend(int deviceType, String json, String product) {
		XingeApp xg = getXingeApp(product);
		Message msg = getMessage(json,product);
		return xg.pushAllDevice(deviceType,msg).getInt(Constants.XINGE_RESULT_CODE);
	}

	/**
	 * 创建大批量推送消息
	 * @Title: createMultipush
	 * @Description:
	 * @param message
	 * @return
	 * JSONObject    返回类型
	 */
	private String createMultipush(String product,Message message)
	{
		XingeApp xg = getXingeApp(product);
		JSONObject obj = xg.createMultipush(message);
		JSONObject res = obj.getJSONObject(Constants.RESULT);
		return res.getString(Constants.PUSH_ID);
	}

	/**
	 * 构建新的信鸽对象
	 * @Title: getXingeApp
	 * @Description:
	 * @param product
	 * @return
	 * XingeApp    返回类型
	 */
	private XingeApp getXingeApp(String product)
	{
		ProductConfig config = ProductConfig.getProductConfig(String.format(Constants.XINGE,product));
		long accessId = Long.valueOf(config.getAccessId());
		XingeApp xg = new XingeApp(accessId,config.getSecretKey());
		return xg;
	}


	/**
	 * 构造Message
	 * @Title: getMessage
	 * @Description:
	 * @param json
	 * @return
	 * Message    返回类型
	 */
	private Message getMessage(String json,String product)
	{
		JSONObject obj =new JSONObject(json);
		JSONObject toJson = obj.getJSONObject(Constants.FIELD_CONTENT);
		ProductConfig config = ProductConfig.getProductConfig(String.format(Constants.XINGE,product));
		toJson.put(Constants.FIELD_DEPLOY_STATUS,config.getDeployStatus());
		Message msg = new Message();
		msg.setContent(toJson.toString());
		//消息离线存储时长
		msg.setExpireTime(1);
		//消息类型 YPE_NOTIFICATION:通知； TYPE_MESSAGE:透传消息
		msg.setType(Message.TYPE_MESSAGE);
		return msg;
	}

	@Override
	public Response<List<String>> sendBatch(SendUserInfo suInfo) {
		com.alibaba.fastjson.JSONObject obj = suInfo.getJson();
		Response<List<String>> res = new Response<>();
		String product = suInfo.getProduct();
		List<String> errTokens = new ArrayList<>();
		res.setCode("0001");
		res.setT(errTokens);
		List<String> tokens;
		List<UserToken> userTokenList;
		int result = 0;
		String token="";
		int uid;
		Map<Integer,String> tokenMap;

		try {
			com.youguu.jms.pojo.Message<com.alibaba.fastjson.JSONObject> msgData = new com.youguu.jms.pojo.Message<>();
			msgData.parse(obj.toJSONString(), com.alibaba.fastjson.JSONObject.class);
			com.alibaba.fastjson.JSONObject json = msgData.getMsg();

			tokens = suInfo.getTokenList();

			//TODO 这里单独处理下股价预警，异动，公告的推送文案
			int type =  json.getJSONObject("content").getIntValue("type");
			if(type == 22 || type == 21)
			{
				com.alibaba.fastjson.JSONObject custom_content =  json.getJSONObject("content").getJSONObject("custom_content");
				String stockName = custom_content.getString("stockname");
				String msgtext = json.getJSONObject("content").getString("description");
				msgtext = msgtext.replace(stockName,"");
				json.getJSONObject("content").put("description",msgtext);
			}

			//标示是否需要统一发送
			int isBatch =  json.getIntValue(Constants.FIELD_BATCH);
			if(isBatch == Constants.BATCH_YES)
			{
				result = batchSend(tokens,json.toJSONString(),product);
				log.info("xinge batchSend  result:{}",result==0?"成功":"失败");
			}
			else
			{
				JSONArray uids = json.getJSONArray(Constants.FIELD_TOUIDS);
				userTokenList = suInfo.getUtList();
				tokenMap = getTokenMap(userTokenList);
				for(int i=0;i<uids.size();i++)
				{
					uid = uids.getIntValue(i);
					com.alibaba.fastjson.JSONObject msg = json.getJSONObject(Constants.FIELD_CONTENT);
					if(msg.containsKey("custom_content")){
						msg.getJSONObject("custom_content").put(Constants.FIELD_RUID, uid);
					}
					token = tokenMap.get(uid);
					log.info("--------------------------"+json.toJSONString());
					result = singleSend(token,json.toJSONString(),product);
					log.info("xinge singleSend user:{} token:{}  result:{}",uid,token,result==0?"成功":"失败");
				}
			}
			res.setCode("0000");
		} catch (Exception e) {
			log.error("xinge send android error",e);
			errTokens.add(token);
		}
		return res;
	}

	private Map<Integer,String> getTokenMap(List<UserToken> userTokenList)
	{
		Map<Integer,String> tokenMap = new HashMap<>();
		for(UserToken ut:userTokenList)
		{
			tokenMap.put(ut.getUserId(),ut.getToken());
		}
		return tokenMap;
	}

	@Override
	public void sendBroadcast(com.alibaba.fastjson.JSONObject msg, String product) {
		try {
			int result = allSend(0, msg.toJSONString(), product);

			if(result != 0)
			{
				flag:for(int i=0;i<3;i++)
				{
					result = allSend(0, msg.toJSONString(), product);
					if(result == 0)
					{
						break flag;
					}
				}
				if(result != 0)
				{
					log.error("send sendBroadcast  errorCode:{}",result);
					log.error("send sendBroadcast  xinGeErrorMsg:{}", PushUtil.getErrorMsgByCode(result));
				}
				else
				{
					log.info("信鸽广播发送成功");
				}
			}
			else
			{
				log.info("信鸽广播发送成功");
			}
		} catch (Exception e) {
			log.error("xinGe sendBroadcast error",e);
		}
	}
}
