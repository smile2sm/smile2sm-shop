package com.smile2sm.mq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MQProducer {

	@Autowired
	AmqpTemplate amqpTemplate;
	
	public void send(String routingKey,Object message) {
		
		try {
			amqpTemplate.convertAndSend(routingKey, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
