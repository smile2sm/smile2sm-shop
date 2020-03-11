package com.smile2sm.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.smile2sm.constant.RedisKey;
import com.smile2sm.entity.SeckillGoods;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class RedisDao {

	@Autowired
	JedisPool jedisPool;
	
	public List<SeckillGoods> getAllSeckillGoods(){
		Jedis jedis = jedisPool.getResource();
		//取出所有seckill_id
		Set<String> set = jedis.smembers("SECKILL_GOODS_IDSET:");
		if(!set.isEmpty()) {
			List<SeckillGoods> list = new ArrayList<>();
			for (String seckill_id:set) {
				//取出对应的json字符串
				String string = jedis.get("SECKILL_GOODS_ID:"+seckill_id);
				SeckillGoods seckillGoods = JSON.parseObject(string, SeckillGoods.class);
				//更新库存
				String seckill_num = jedis.get(RedisKey.SECKILL_STOCK + seckill_id);
				seckillGoods.setSeckill_num(Integer.parseInt(seckill_num));
				
				list.add(seckillGoods);
			}
		}
		jedis.close();
		return null;
	}
	
	
	public void setAllSeckillGoods(List<SeckillGoods> listSeckillGoods){

		if(StringUtils.isEmpty(listSeckillGoods)) return;
		
		Jedis jedis = jedisPool.getResource();
		for (int i = 0; i < listSeckillGoods.size(); i++) {
			SeckillGoods seckillGoods = listSeckillGoods.get(i);
			String jsonString = JSON.toJSONString(seckillGoods);
			//设置redis缓存数据
			jedis.set("SECKILL_GOODS_ID:"+seckillGoods.getSeckill_id(), jsonString);
			jedis.set(RedisKey.SECKILL_STOCK + seckillGoods.getSeckill_id(), ""+seckillGoods.getSeckill_num());
			jedis.sadd("SECKILL_GOODS_IDSET:", seckillGoods.getSeckill_id()+"");
		}
		jedis.close();
	}
}
