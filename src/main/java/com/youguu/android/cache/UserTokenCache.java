package com.youguu.android.cache;

import com.youguu.android.util.Constants;
import com.youguu.core.util.ParamUtil;
import com.youguu.core.zookeeper.pro.ZkPropertiesHelper;

import java.util.*;

/**
 * 
 * @ClassName: UserTokenCache
 * @Description: 缓存用户的token - 广播的时候使用
 * @author wangdong
 * @date 2015年6月27日 上午10:04:22
 *
 */
public class UserTokenCache {
	
	/**
	 * 初始化是否完成
	 */
	private static boolean initOk;
	
	/**
	 * 指定天数之前的token 不发送消息通知
	 */
	private static int day;
	
	/**
	 * 当前进程加载token的最小id  >  拆分进程广播使用
	 */
	private static int conf_minId;
	
	/**
	 * 当前进程加载token的最大id <=  拆分进程广播使用
	 */
	private static int conf_maxId;
	
	static{
		// 读取配置文件
		Properties pro = ZkPropertiesHelper.getCacheAndWatchProperties(Constants.CONFIG_PATH, true);
		
		day = ParamUtil.CheckParam(pro.getProperty(Constants.DAY), 365);
		//当前进程发送广播的最小id
		conf_minId = ParamUtil.CheckParam(pro.getProperty("minId"), 0);
		
		maxId = conf_minId;
		
		//当前进程发送广播的最大id
		conf_maxId = ParamUtil.CheckParam(pro.getProperty("maxId"), Integer.MAX_VALUE);
	}
	
	/**
	 * 当前最大id
	 */
	private static int maxId;
	
	public static int getMaxId() {
		return maxId;
	}

	public static void setMaxId(int maxId) {
		UserTokenCache.maxId = maxId;
	}

	/**
	 * 用户的token key:productId
	 */
	private static Map<String,Set<String>> userTokens =new HashMap<>();
	
	/**
	 * 
	* @Title: addToken
	* @Description: 添加用户token缓存
	* @param @param product
	* @param @param token    
	* @return void    返回类型
	* @throws
	 */
	public static void addToken(String product,String token,int pushServer) {
		String key = product+"_"+pushServer;
		Set<String> set  = userTokens.get(key);
		if(set==null){
			set = new LinkedHashSet<>(10000);
			userTokens.put(key,set);
		}
		userTokens.get(key).add(token);
	}


	
	/**
	 * 
	* @Title: removeToken
	* @Description: 删除无效的token
	* @param @param product
	* @param @param tokens    
	* @return void    返回类型
	* @throws
	 */
	public static void removeToken(String product,int pushServer,List<String> tokens){
		synchronized (UserTokenCache.class) {
			String key = product+"_"+pushServer;
			Set<String> set  = userTokens.get(key);
			if(set!=null){
				for(String token:tokens){
					set.remove(token);
				}
			}
		}
		
	}
	
	public static Set<String> getProductTokens(String product,int pushServer){
		String key = product+"_"+pushServer;
		return userTokens.get(key);
	}
	
	/**
	 * 
	* @Title: isInitOk
	* @Description: 判断初始化是否完成
	* @param @return    
	* @return boolean    返回类型
	* @throws
	 */
	public static boolean isInitOk(){
		return initOk;
	}
	
	/**
	 * 
	* @Title: setInitOk
	* @Description: 设置初始化完成
	* @param     
	* @return void    返回类型
	* @throws
	 */
	public static void setInitOk(){
		initOk = true;
	}

	public static int getDay() {
		return day;
	}

	public static int getConf_minId() {
		return conf_minId;
	}

	public static int getConf_maxId() {
		return conf_maxId;
	}
	
	
}
