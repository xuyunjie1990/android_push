package com.youguu.android.util;

/**
 * 推送工具类
 * @Title: PushUtil.java
 * @Package com.youguu.android.util
 * @Description:
 * @author 徐云杰
 * @date 2015年7月30日 下午3:16:17
 * @version V1.0
 */
public class PushUtil {

	/**
	 * 校验返回Code，如果错误原因为token错误则返回true
	 * @Title: verifyXinGeResultCode
	 * @Description:
	 * @param code
	 * @return    
	 * boolean    返回类型
	 */
	public static boolean verifyXinGeResultCode(int code)
	{
		boolean result = false;
		switch (code) {
		case 14:
			//收到非法 token，例如 ios 终端没能拿到正确的 token， 请检查 token 是否正确
			result=true;
			break;
		case 40:
			//推送的 token 没有在信鸽中注册，或者推送的帐号没有绑定 token， 请检查注册逻辑
			result=true;
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * 通过Code获取对应的异常信息
	 * @Title: getErrorMsgByCode
	 * @Description:
	 * @return    
	 * String    返回类型
	 */
	public static String getErrorMsgByCode(int code)
	{
		String result = "";
		switch (code) {
		case -1:
			result = "参数错误，请对照文档检查请求参数";
			break;
		case -2:
			result = "请求时间戳不在有效期内，检查请求的时间戳设置是否正确，机器时间是否正确";
			break;
		case -3:
			result = "sign校验无效，检查Access ID和Secret Key（注意不是Access Key）";
			break;
		case 2:
			result = "参数错误";
			break;
		case 7:
			result = "别名/账号绑定的终端数满了（10个），请解绑部分终端";
			break;
		case 14:
			result = "收到非法token，例如ios终端没能拿到正确的token，请检查token是否正确";
			break;
		case 15:
			result = "信鸽逻辑服务器繁忙，请稍后再试";
			break;
		case 19:
			result = "操作时序错误，例如进行tag操作前未获取到deviceToken 没有获取到deviceToken的原因: 1.没有注册信鸽或者苹果推送。 2.provisioning profile制作不正确。";
			break;
		case 20:
			result = "鉴权错误，请检查access id和access key是否正确";
			break;
		case 40:
			result = "推送的token没有在信鸽中注册，或者推送的账号没有绑定token，请检查注册逻辑";
			break;
		case 48:
			result = "推送的账号没有在信鸽中注册，请检查注册逻辑";
			break;
		case 63:
			result = "标签系统忙，请稍后再试";
			break;
		case 71:
			result = "APNS服务器繁忙，请稍后再试";
			break;
		case 73:
			result = "消息字符数超限，请缩小消息内容";
			break;
		case 76:
			result = "请求过于频繁，请稍后再试";
			break;
		case 78:
			result = "循环任务参数错误，请对照文档检查请求参数";
			break;
		case 100:
			result = "APNS证书错误。请重新提交正确的证书";
			break;
		default:
			result = "内部错误";
			break;
		}
		return result;
	}

}
