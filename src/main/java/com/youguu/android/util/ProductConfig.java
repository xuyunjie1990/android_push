package com.youguu.android.util;

import com.youguu.core.zookeeper.pro.ZkPropertiesHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 产品对应的信息
 * @Title: ProductConfig.java
 * @Package com.youguu.android.util
 * @Description: 
 * @author 徐云杰
 * @date 2015年7月30日 上午10:44:19
 * @version V1.0
 */
public class ProductConfig {
	private final static String CONNFILEPATH = "%s.properties"; 

	private static Map<String, ProductConfig> pro_map = new HashMap<>();
	/**
	 * 应用ID
	 */
	private String accessId;

	/**
	 * 服务器端key
	 */
	private String secretKey;

	/**
	 *  包名
	 */
	private String packageName;

	/**
	 * 环境 1：测试  2：线上
	 */
	private int deployStatus;

	/**
	 * 华为路由连接数
	 */
	private int connNum;

	/**
	 * 华为路游最大连接数
	 */
	private int connMaxNum;

	/**
	 * 配置文件路径
	 */
	private String propPath;

	/**
	 * 配置文件密码
	 */
	private String propPwd;

	public static ProductConfig getProductConfig(String product){
		ProductConfig config = pro_map.get(product);

		if(config!=null) return config;

		synchronized (pro_map) {
			config = pro_map.get(product);
			if(config==null){
				config = new ProductConfig();
				try {
					Properties properties = ZkPropertiesHelper.getCacheAndWatchProperties(String.format(CONNFILEPATH,product),true);
					config.accessId = properties.getProperty("access_id","");
					config.secretKey = properties.getProperty("secret_key","");
					config.packageName = properties.getProperty("package_name","");
					config.deployStatus = Integer.valueOf(properties.getProperty("deploy_status", "0"));
					config.connNum = Integer.valueOf(properties.getProperty("connNum", "50"));
					config.connMaxNum = Integer.valueOf(properties.getProperty("connMaxNum", "100"));
					config.propPath = String.valueOf(properties.getProperty("prop_path", "/properties/mykeystorebj.jks"));
					config.propPwd = String.valueOf(properties.getProperty("prop_pwd", "123456"));
					pro_map.put(product, config);
				} catch (Exception e) {
					e.printStackTrace();
					throw new ExceptionInInitializerError(e);
				}
			}
		}
		return config;
	}

	public static String getConnfilepath() {
		return CONNFILEPATH;
	}

	public static Map<String, ProductConfig> getPro_map() {
		return pro_map;
	}

	public String getAccessId() {
		return accessId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getPackageName() {
		return packageName;
	}

	public int getDeployStatus() {
		return deployStatus;
	}

	public int getConnNum() {
		return connNum;
	}

	public int getConnMaxNum() {
		return connMaxNum;
	}

	public String getPropPath() {
		return propPath;
	}

	public String getPropPwd() {
		return propPwd;
	}

	@Override
	public String toString() {
		return "ProductConfig{" +
				"accessId='" + accessId + '\'' +
				", secretKey='" + secretKey + '\'' +
				", deployStatus=" + deployStatus +
				", connNum=" + connNum +
				", connMaxNum=" + connMaxNum +
				", propPath='" + propPath + '\'' +
				", propPwd='" + propPwd + '\'' +
				'}';
	}
}
