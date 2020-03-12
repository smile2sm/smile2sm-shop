package com.smile2sm.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.smile2sm.config.MQConfig;
import com.smile2sm.constant.RedisKey;
import com.smile2sm.dao.PayOrderDao;
import com.smile2sm.dao.RedisDao;
import com.smile2sm.dao.SeckillGoodsDao;
import com.smile2sm.dto.SeckillExposer;
import com.smile2sm.entity.SeckillGoods;
import com.smile2sm.enums.SeckillStateEnum;
import com.smile2sm.exception.SeckillException;
import com.smile2sm.mq.MQProducer;
import com.smile2sm.service.SeckillGoodsService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	JedisPool jedisPool;
	
	@Autowired
	SeckillGoodsDao seckillGoodsDao;
	
	@Autowired
	PayOrderDao payOrderDao;
	
	@Autowired
	RedisDao redisDao;
	
	@Autowired
	MQProducer mQProducer;
	
	@Override
	public List<SeckillGoods> listSeckillGoods() {
		List<SeckillGoods> listSeckillGoods = redisDao.getAllSeckillGoods();
		if(!StringUtils.isEmpty(listSeckillGoods)) return listSeckillGoods;
		
		listSeckillGoods = seckillGoodsDao.listSeckillGoods();
		redisDao.setAllSeckillGoods(listSeckillGoods);
		
		return listSeckillGoods;
	}

	/**
	 * 查看单个商品详细
	 */
	@Override
	public SeckillGoods getSeckillGoodsDetail(long seckill_id) {
		
		Jedis jedis = jedisPool.getResource();
		String string = jedis.get(RedisKey.SECKILL_ID+seckill_id);
		
		if(!StringUtils.isEmpty(string)) {
			SeckillGoods seckillGoods = JSON.parseObject(string, SeckillGoods.class);
			String seckill_num = jedis.get(RedisKey.SECKILL_STOCK+seckill_id);
			seckillGoods.setSeckill_num(Integer.parseInt(seckill_num));
			jedis.close();
			return seckillGoods;
		}
		return null;
	}
	/**
	 * 暴露秒杀地址
	 */
	@Override
	public SeckillExposer exposer(long seckill_id) {
		
		//读取出redis中对应seckill_id的商品数据
		Jedis jedis = jedisPool.getResource();
		String string = jedis.get(RedisKey.SECKILL_ID+seckill_id);
		jedis.close();
		//数据为空，秒杀没准备好或者数据已被删除
		if(StringUtils.isEmpty(string)) return new SeckillExposer(false, seckill_id);
		
		SeckillGoods seckillGoods = JSON.parseObject(string, SeckillGoods.class);
		long start_time = seckillGoods.getSeckill_start_time().getTime();
		long end_time = seckillGoods.getSeckill_end_time().getTime();
		long now = new Date().getTime();
		
		//秒杀未开始或者秒杀已结束
		if (start_time > now || end_time < now) {
			return new SeckillExposer(false, seckill_id, start_time, end_time, now);
		}
		
		return new SeckillExposer(true, seckill_id,getMd5());
	}
	
	/**
	 * 执行秒杀
	 */
	public SeckillStateEnum executeSeckill(long seckill_id, String phone){
		
		Jedis jedis = jedisPool.getResource();
		
		String StockStr = jedis.get(RedisKey.SECKILL_STOCK + seckill_id);
		
		int skillStock = Integer.valueOf(StockStr);
		
		if (skillStock <= 0) {
			jedis.close();
			logger.info("SECKILL_OUT. seckill_id={},phone={}", seckill_id, phone);
			return SeckillStateEnum.SECKILL_OUT;
		}
		
		if (jedis.sismember(RedisKey.SECKILL_USERS + seckill_id, String.valueOf(phone))) {
			jedis.close();
			// 重复秒杀
			logger.info("SECKILL_REPEAT. seckill_id={},phone={}", seckill_id, phone);
			return SeckillStateEnum.SECKILL_REPEAT;
		} else {
			jedis.close();
			// 进入待秒杀队列，进行后续串行操作
			logger.info("SECKILL_QUEUE. seckill_id={},phone={}", seckill_id, phone);
			
			mQProducer.send(MQConfig.SECKILL_QUEUE, seckill_id+","+phone);

			return SeckillStateEnum.SECKILL_QUEUE;
		}
	}


	/**
	 * 在Redis中进行秒杀处理
	 */
	@Override
	public void handleInRedis(long seckill_id, String phone) throws SeckillException {
		Jedis jedis = jedisPool.getResource();

		String stockKey = RedisKey.SECKILL_STOCK + seckill_id;
		String boughtKey = RedisKey.SECKILL_USERS + seckill_id;

		String stockStr = jedis.get(stockKey);
		int skillStock = Integer.valueOf(stockStr);
		
		//判断是否重复秒杀
		if (jedis.sismember(boughtKey, phone)) {
			jedis.close();
			logger.info("handleInRedis SECKILL_REPEAT. seckill_id={},phone={}", seckill_id, phone);
			throw new SeckillException(SeckillStateEnum.SECKILL_REPEAT);
		}
		//判断是否还有库存
		if (skillStock <= 0) {
			jedis.close();
			logger.info("handleInRedis SECKILL_OUT. seckill_id={},phone={}", seckill_id, phone);
			throw new SeckillException(SeckillStateEnum.SECKILL_OUT);
		}
		//存在问题：当前库存减一，而添加秒杀成功没有时失败了该怎么处理
		jedis.decr(stockKey);
		jedis.sadd(boughtKey, String.valueOf(phone));
		jedis.close();
		logger.info("handleInRedis SECKILL_SUCCESS. seckill_id={},phone={}", seckill_id, phone);
	}

	
	/**
    * 定时轮询秒杀结果
    */
   @Override
   public SeckillStateEnum isGrab(long seckill_id, String phone) {

       Jedis jedis = jedisPool.getResource();
       try {
           String boughtKey = RedisKey.SECKILL_USERS + seckill_id;
           if(jedis.sismember(boughtKey,phone)){
        	   return SeckillStateEnum.SECKILL_SUCCESS;
           };
       } catch (Exception ex) {
           logger.error(ex.getMessage(), ex);
       }
       
       //问题：如何判断没有秒杀到呢
       return SeckillStateEnum.SECKILL_QUEUE;
   }
	
	
	
	
	
	/**
	 * 先插入秒杀记录再减库存
	 */
	@Override
	@Transactional
	public void updateStock(long seckill_id, String phone) throws SeckillException {
		Jedis jedis = jedisPool.getResource();
		String boughtKey = RedisKey.SECKILL_USERS + seckill_id;
		//判断是否秒杀到
		if (!jedis.sismember(boughtKey, phone)) {
			jedis.close();
			logger.info("updateStock ORDER_ERROR. seckill_id={},phone={}", seckill_id, phone);
			//没秒杀到，异常订单
			throw new SeckillException(SeckillStateEnum.ORDER_ERROR);
		}
		jedis.close();
		
//		//判断是否已经插入订单
//		PayOrder payOrder = payOrderDao.getPayOrder(seckill_id, phone);
//		if(!StringUtils.isEmpty(payOrder)) {
//			//已经创建订单
//			logger.info("updateStock CREATE_ORDER_SUCCESS. seckill_id={},phone={}", seckill_id, phone);
//			return ;
//		}
//		
		//插入订单
		int result = payOrderDao.insertPayOrder(seckill_id, phone,1, new Date());
		//创建订单失败
		if(result != 1) {
			logger.info("updateStock CREATE_ORDER_ERROR. seckill_id={},phone={}", seckill_id, phone);
			throw new SeckillException(SeckillStateEnum.CREATE_ORDER_ERROR);
		}
		//减库存
		result = seckillGoodsDao.reduceSeckillNum(seckill_id);
		if(result != 1) {
			logger.info("updateStock reduceSeckillNum. seckill_id={},phone={}", seckill_id, phone);
			throw new SeckillException(SeckillStateEnum.CREATE_ORDER_ERROR);
		}
		
		logger.info("updateStock CREATE_ORDER_SUCCESS. seckill_id={},phone={}", seckill_id, phone);
	}

}
