package com.smile2sm.mq;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smile2sm.config.MQConfig;
import com.smile2sm.config.MqConfig;
import com.smile2sm.constant.RedisKey;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class MQConsumer {

	@Autowired
	AmqpTemplate amqpTemplate;
	
	@Autowired
	JedisPool jedisPool;
	
	@RabbitListener(queues = MQConfig.SECKILL_QUEUE)
	public void receive(String msgBody) {
		
		String[] split = msgBody.split(",");
		Jedis jedis = jedisPool.getResource();
		String skillStockKey = RedisKey.SECKILL_STOCK + split[0];
		Long decr = jedis.decr(skillStockKey);
		if(decr <= 0) {
			return;
		}
		
		jedis.set("", "");
	}
}
