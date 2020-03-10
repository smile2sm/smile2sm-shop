package com.smile2sm.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class MqConfig {

	public final static String QUEUE = "queue1";
	
	@Bean
	public Queue queue() {
		return new Queue(QUEUE);
	}
}
