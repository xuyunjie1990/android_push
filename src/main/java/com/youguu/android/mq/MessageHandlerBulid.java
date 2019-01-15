package com.youguu.android.mq;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.youguu.android.util.SpringUtil;
import com.youguu.core.util.AnnotationScan;
import com.youguu.jms.message.handler.HandlerBuilder;
import com.youguu.jms.message.handler.HandlerType;
import com.youguu.jms.message.handler.IMessageHandler;
import com.youguu.jms.message.handler.MessageHandlerProvider;


@HandlerBuilder
public class MessageHandlerBulid {
	public void build(){
		AnnotationScan pp = AnnotationScan.getInstance();
		Set<Class<?>> set = pp.getPackageClass("com.youguu.android.mq.impl", HandlerType.class, true);
		for(Class<?> clazz:set){
			HandlerType ht = clazz.getAnnotation(HandlerType.class);
			int type = ht.type();
			String destination = ht.destination();
			
			try {
				Service service = (Service)clazz.getAnnotation(Service.class);
				IMessageHandler handler = null;
				if(service!=null){
					handler = (IMessageHandler)SpringUtil.getBean(service.value());
				}else{
					handler = (IMessageHandler)clazz.newInstance();
				}
				MessageHandlerProvider.put(destination, type, handler);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	
		
	}
	
}
