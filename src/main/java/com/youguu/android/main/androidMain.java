package com.youguu.android.main;

import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.youguu.android.util.ContextLoader;
import com.youguu.core.zookeeper.pro.ZkPropertiesHelper;
import com.youguu.jms.handler.IMqHandler;
import com.youguu.jms.handler.MqHandlerProvider;

public class androidMain {
	public static void main(String[] args){
		
		ApplicationContext appCtxnew  = new AnnotationConfigApplicationContext(ContextLoader.class);
		
		//启动mq的监听
		try {
			Properties p = ZkPropertiesHelper.getCacheAndWatchProperties("task.properties",true);
			String[] tasks = p.getProperty("task").split(",");
			for(String destination:tasks){
				IMqHandler handler = MqHandlerProvider.get(destination);
				handler.recevie();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
