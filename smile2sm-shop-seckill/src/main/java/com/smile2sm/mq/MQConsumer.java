package com.smile2sm.mq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smile2sm.config.MQConfig;
import com.smile2sm.service.SeckillGoodsService;

@Component
public class MQConsumer {

	@Autowired
	AmqpTemplate amqpTemplate;
	
	@Autowired
	SeckillGoodsService seckillGoodsService;
	
	@RabbitListener(queues = MQConfig.SECKILL_QUEUE)
	public void receive(String msgBody) {
		
		String[] split = msgBody.split(",");
		try {
			seckillGoodsService.handleInRedis(Long.parseLong(split[0]), split[1]);
		}catch (Exception e) {
			
		}
	}
}
