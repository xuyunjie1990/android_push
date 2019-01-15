package com.youguu.android.mq.impl.batch;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.youguu.android.mq.impl.MessageHandlerBasic;
import com.youguu.android.util.Constants;
import com.youguu.android.util.UserToken;
import com.youguu.jms.message.handler.HandlerType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @Title: MncgBatchHandler.java
 * @Package com.youguu.android.mq.impl.batch
 * @Description:
 * {
 * 		"content":{
 * 			title:""
 * 			msg:""
 * 		}, android推送的格式  直接处理
 * 		"product":"",
 * 		"toUids":"",
 * 		"batch"1  //1表示所有人的消息一致 ， 非他表示需要单独的touid
 * }
 * //TODO 队列消息消费慢 造成对象释放过慢 内存增加
 * @author 徐云杰
 * @date 2015年8月5日 上午10:10:47
 * @version V1.0
 */
@HandlerType(destination=Constants.QUEUE_MNCG_BATCH,type=1)
@Service("mncgBatchHandler")
public class MncgBatchHandler extends MessageHandlerBasic {

	@Resource
	private DisposeHandler disposeHandler;
	
	@Override
	public void handler(JSONObject json) {
		log.info("MncgBatchHandler receive msg:" + json.toJSONString());
		try{
			JSONArray array = json.getJSONArray(com.youguu.jms.Constants.BATCH_MSG);
			//获取所有用户ID
			Set<Integer> set = disposeHandler.getAllUserId(array);

			//查询数据库获取用户所有信息
			Map<Integer,UserToken> allInfo = disposeHandler.getAllUserToken(set, Constants.PRODUCT_MNCG);

			if (array != null && array.size() > 0)
			{
				JSONObject jsonObject;
				for(int i =0; i< array.size(); i ++)
				{
					jsonObject = array.getJSONObject(i);

					//解析json，返回用户ID集合
					List<Integer> userIds = disposeHandler.parseJson(jsonObject);

					if(userIds == null || userIds.size()==0)
					{
						continue;
					}

					//对用户token进行分服务处理
					Map<Integer,List<UserToken>> utMap = new HashMap<>();
					Map<Integer,List<String>> tokenMap = disposeHandler.getTokenMap(userIds, utMap, allInfo);

					//分配任务
					disposeHandler.allocatingTask(tokenMap, jsonObject, utMap, Constants.PRODUCT_MNCG);
				}
			}
		}catch(Exception e){
			log.error("处理群发消息失败:" +json.toJSONString() , e);
		}

	}
}
