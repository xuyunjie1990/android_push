package com.youguu.android.service;

import com.alibaba.fastjson.JSONObject;
import com.youguu.android.util.SendUserInfo;
import com.youguu.core.pojo.Response;

import java.util.List;

/**
 * 
 * @ClassName: ImessageSendService
 * @Description: 安卓消息发送
 * @author wangdong
 * @date 2015年6月27日 下午1:23:05
 *
 */
public interface ImessageSendService {

	/**
	 * 批量发送消息  
	 * @Title: sendBatch
	 * @Description: 
	 * @param @param msg
	 * @param @param product
	 * @param @return    
	 * @return List<String>    返回类型 失效的token
	 * @throws
	 */
	Response<List<String>> sendBatch(SendUserInfo suInfo);

	/**
	 * 广播
	 * @Title: sendBroadcast
	 * @Description:
	 * @param msg
	 * @param product
	 * @return    
	 * Response<List<String>>    返回类型
	 */
	void sendBroadcast(JSONObject msg,String product);
}
