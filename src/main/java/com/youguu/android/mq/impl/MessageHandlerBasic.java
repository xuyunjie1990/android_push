package com.youguu.android.mq.impl;

import com.youguu.core.logging.Log;
import com.youguu.core.logging.LogFactory;
import com.youguu.jms.message.handler.HandlerType;
import com.youguu.jms.message.handler.IMessageHandler;

public abstract class MessageHandlerBasic implements IMessageHandler {
	protected Log log = null;
	public MessageHandlerBasic(){
		HandlerType ht = this.getClass().getAnnotation(HandlerType.class);
		log = LogFactory.getLog(ht.destination());
	}

}
