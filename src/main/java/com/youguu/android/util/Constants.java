package com.youguu.android.util;

/**
 * 常量配置
 * @Title: Constants.java
 * @Package com.youguu.android.util
 * @Description: 
 * @author 徐云杰
 * @date 2015年7月29日 下午4:51:11
 * @version V1.0
 */
public class Constants {

	/*****************队列名称定义*****************/

	/**
	 * 模拟炒股批量队列
	 */
	public static final String QUEUE_MNCG_BATCH = "android_mncg_batch";
	
	/**
	 * 模拟炒股广播
	 */
	public static final String QUEUE_MNCG_BROADCAST = "android_mncg_broadcast";
	
	/**
	 * 优顾理财批量队列
	 */
	public static final String QUEUE_YGLC_BATCH = "android_yglc_batch";
	
	/**
	 * 优顾理财广播
	 */
	public static final String QUEUE_YGLC_BROADCAST = "android_yglc_broadcast";


	/*****************常量定义*****************/
	public static String FIELD_TOUIDS = "toUids";

	public static String FIELD_TOKENS = "toTokens";

	public static String FIELD_UID_AND_TOKEN = "uidAndToken";
	
	public static String FIELD_BATCH = "batch";
	
	public static final int BATCH_YES = 1;
	public static final int BATCH_NO = 0;

	public static final int GUANGBO_STATUS_YES = 0;
	public static final int GUANGBO_STATUS_NO = 1;

	public final static String PRODUCT_MNCG = "001";

	public final static String PRODUCT_YGLC = "002";
	
	public final static String FIELD_MSG = "msg";
	
	public final static String FIELD_TITLE = "title";
	
	public final static String FIELD_CONTENT = "content";
	
	public static String FIELD_RUID = "ruid";
	
	public final static String XINGE = "xinge_%s";
	
	public final static String XINGE_TITLE = "有一条新信息";
	
	public final static String XINGE_RESULT_CODE = "ret_code";
	
	public final static String FIELD_DEPLOY_STATUS = "deploy_status";

	public final static String HUAWEI = "huawei_%s";

	public final static String XIAOMI = "xiaomi_%s";
	
	/**
	 * 批量推送ID标识
	 */
	public final static String PUSH_ID = "push_id";
	
	public final static String RESULT = "result";

	/**
	 * 配置信息路径
	 */
	public final static String CONFIG_PATH = "config.properties";

	/**
	 * 过滤的天数
	 */
	public final static String DAY="day";

}
