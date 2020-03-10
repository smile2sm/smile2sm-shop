package com.smile2sm.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.smile2sm.config.MQConfig;
import com.smile2sm.config.MqConfig;
import com.smile2sm.constant.RedisKey;
import com.smile2sm.dao.SeckillGoodsDao;
import com.smile2sm.dto.SeckillMsgBody;
import com.smile2sm.dto.SeckillState;
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
	MQProducer sender;
	
	

	@Override
	public List<SeckillGoods> listSeckillGoods() {
		Jedis jedis = jedisPool.getResource();
		
		Set<String> set = jedis.smembers("SECKILL_GOODS_IDSET");
		if(!set.isEmpty()) {
			List<SeckillGoods> list = new ArrayList<>();
			for (String seckill_id:set) {
				String string = jedis.get("SECKILL_GOODS_ID:"+seckill_id);
				SeckillGoods seckillGoods = JSON.parseObject(string, SeckillGoods.class);
				list.add(seckillGoods);
			}
			jedis.close();
			return list;
		}
		
		
		List<SeckillGoods> listSeckillGoods = seckillGoodsDao.listSeckillGoods();
		
		for (int i = 0; i < listSeckillGoods.size(); i++) {
			SeckillGoods seckillGoods = listSeckillGoods.get(i);
			String jsonString = JSON.toJSONString(seckillGoods);
			
			jedis.set("SECKILL_GOODS_ID:"+seckillGoods.getSeckill_id(), jsonString);
			jedis.sadd("SECKILL_GOODS_IDSET", seckillGoods.getSeckill_id()+"");
		}
		jedis.close();
		return listSeckillGoods;
	}

	@Override
	public SeckillGoods getSeckillGoodsDetail(long seckill_id) {
		
		Jedis jedis = jedisPool.getResource();
		String string = jedis.get("SECKILL_GOODS_ID:"+seckill_id);
		
		if(!StringUtils.isEmpty(string)) {
			SeckillGoods seckillGoods = JSON.parseObject(string, SeckillGoods.class);
			
			jedis.set(RedisKey.SECKILL_STOCK+seckill_id, seckillGoods.getSeckill_num()+"");
			jedis.close();
			return JSON.parseObject(string, SeckillGoods.class);
		}
		jedis.close();
		return seckillGoodsDao.getGoodsDetail(seckill_id);
	}

	@Override
	public SeckillGoods getSeckillUrl(long goods_id) {
		return null;
	}

	/**
	 * 执行秒杀
	 */
	public SeckillState executeSeckill(long seckillId, String phone) throws SeckillException {
		
		Jedis jedis = jedisPool.getResource();
		
		String skillStockKey = RedisKey.SECKILL_STOCK + seckillId;
		String skillUserKey = RedisKey.SECKILL_USERS + seckillId;

		String StockStr = jedis.get(skillStockKey);
		
		int skillStock = Integer.valueOf(StockStr==null?"0":StockStr);
		if (skillStock <= 0) {
			jedis.close();
			logger.info("SECKILLSOLD_OUT. seckillId={},userPhone={}", seckillId, phone);
			throw new SeckillException(SeckillStateEnum.SECKILL_OUT);
		}
		
		if (jedis.sismember(skillUserKey, String.valueOf(phone))) {
			jedis.close();
			// 重复秒杀
			logger.info("SECKILL_REPEATED. seckillId={},userPhone={}", seckillId, phone);
			throw new SeckillException(SeckillStateEnum.REPEAT_KILL);
		} else {
			jedis.close();
			// 进入待秒杀队列，进行后续串行操作
			sender.send(MQConfig.SECKILL_QUEUE, seckillId+","+phone);

			return new SeckillState(SeckillStateEnum.ENQUEUE_PRE_SECKILL);
		}
	}


	/**
	 * 在Redis中真正进行秒杀操作
	 * 
	 * @param seckillId
	 * @param userPhone
	 * @throws SeckillException
	 */
//	@Override
//	public void handleInRedis(long seckillId, long userPhone) throws SeckillException {
//		Jedis jedis = jedisPool.getResource();
//
//		String inventoryKey = RedisKeyPrefix.SECKILL_INVENTORY + seckillId;
//		String boughtKey = RedisKeyPrefix.BOUGHT_USERS + seckillId;
//
//		String inventoryStr = jedis.get(inventoryKey);
//		int inventory = Integer.valueOf(inventoryStr);
//		if (inventory <= 0) {
//			logger.info("handleInRedis SECKILLSOLD_OUT. seckillId={},userPhone={}", seckillId, userPhone);
//			throw new SeckillException(SeckillStateEnum.SOLD_OUT);
//		}
//		if (jedis.sismember(boughtKey, String.valueOf(userPhone))) {
//			logger.info("handleInRedis SECKILL_REPEATED. seckillId={},userPhone={}", seckillId, userPhone);
//			throw new SeckillException(SeckillStateEnum.REPEAT_KILL);
//		}
//		jedis.decr(inventoryKey);
//		jedis.sadd(boughtKey, String.valueOf(userPhone));
//		logger.info("handleInRedis_done");
//	}
//
//	/**
//	 * 先插入秒杀记录再减库存
//	 */
//	@Override
//	@Transactional
//	public SeckillExecution updateInventory(long seckillId, long userPhone) throws SeckillException {
//		// 执行秒杀逻辑:减库存 + 记录购买行为
//		Date nowTime = new Date();
//		try {
//			// 插入秒杀记录(记录购买行为)
//			// 这处， seckill_record的id等于这个特定id的行被启用了行锁, 但是其他的事务可以insert另外一行，
//			// 不会阻止其他事务里对这个表的insert操作
//			int insertCount = payOrderDAO.insertPayOrder(seckillId, userPhone, nowTime);
//			// 唯一:seckillId,userPhone
//			if (insertCount <= 0) {
//				// 重复秒杀
//				logger.info("seckill REPEATED. seckillId={},userPhone={}", seckillId, userPhone);
//				throw new SeckillException(SeckillStateEnum.REPEAT_KILL);
//			} else {
//				// 减库存,热点商品竞争
//				// reduceNumber是update操作，开启作用在表seckill上的行锁
//				Seckill currentSeckill = seckillDAO.queryById(seckillId);
//				boolean validTime = false;
//				if (currentSeckill != null) {
//					long nowStamp = nowTime.getTime();
//					if (nowStamp > currentSeckill.getStartTime().getTime()
//							&& nowStamp < currentSeckill.getEndTime().getTime() && currentSeckill.getInventory() > 0
//							&& currentSeckill.getVersion() > -1) {
//						validTime = true;
//					}
//				}
//
//				if (validTime) {
//					long oldVersion = currentSeckill.getVersion();
//					// update操作开始，表seckill的seckill_id等于seckillId的行被启用了行锁, 其他的事务无法update这一行，
//					// 可以update其他行
//					int updateCount = seckillDAO.reduceInventory(seckillId, oldVersion, oldVersion + 1);
//					if (updateCount <= 0) {
//						// 没有更新到记录，秒杀结束,rollback
//						logger.info("seckill_DATABASE_CONCURRENCY_ERROR!!!. seckillId={},userPhone={}", seckillId,
//								userPhone);
//						throw new SeckillException(SeckillStateEnum.DB_CONCURRENCY_ERROR);
//					} else {
//						// 秒杀成功 commit
//						PayOrder payOrder = payOrderDAO.queryByIdWithSeckill(seckillId, userPhone);
//						logger.info("seckill SUCCESS->>>. seckillId={},userPhone={}", seckillId, userPhone);
//						return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, payOrder);
//						// return后，事务结束，关闭作用在表seckill上的行锁
//						// update结束，行锁被取消 。reduceInventory()被执行前后数据行被锁定, 其他的事务无法写这一行。
//					}
//				} else {
//					logger.info("seckill_END. seckillId={},userPhone={}", seckillId, userPhone);
//					throw new SeckillException(SeckillStateEnum.END);
//				}
//			}
//		} catch (SeckillException e1) {
//			throw e1;
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//			// 所有编译期异常 转化为运行期异常
//			throw new SeckillException(SeckillStateEnum.INNER_ERROR);
//		}
//	}

}
