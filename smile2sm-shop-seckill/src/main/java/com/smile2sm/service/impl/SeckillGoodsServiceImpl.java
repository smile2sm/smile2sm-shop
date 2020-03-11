package com.smile2sm.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.smile2sm.config.MQConfig;
import com.smile2sm.constant.RedisKey;
import com.smile2sm.dao.RedisDao;
import com.smile2sm.dao.SeckillGoodsDao;
import com.smile2sm.dto.SeckillState;
import com.smile2sm.entity.SeckillExposer;
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
		String string = jedis.get("SECKILL_GOODS_ID:"+seckill_id);
		
		if(!StringUtils.isEmpty(string)) {
			SeckillGoods seckillGoods = JSON.parseObject(string, SeckillGoods.class);
			String seckill_num = jedis.get(RedisKey.SECKILL_STOCK+seckill_id);
			seckillGoods.setSeckill_num(Integer.parseInt(seckill_num));
			jedis.close();
			return seckillGoods;
		}
		return null;
	}
	@Override
	public SeckillExposer exposer(long seckill_id) {
		Jedis jedis = jedisPool.getResource();
		String string = jedis.get("SECKILL_GOODS_ID:"+seckill_id);
		jedis.close();
		
		if(StringUtils.isEmpty(string)) return new SeckillExposer(false, seckill_id);
		
		SeckillGoods seckillGoods = JSON.parseObject(string, SeckillGoods.class);
		long start_time = seckillGoods.getSeckill_start_time().getTime();
		long end_time = seckillGoods.getSeckill_end_time().getTime();
		long now = new Date().getTime();
		
		//秒杀未开始或者秒杀已结束
		if (start_time > now || end_time < now) {
			return new SeckillExposer(false, seckill_id, start_time, end_time, now);
		}
		
		return new SeckillExposer(true, seckill_id,"executeSeckill");
	}
	
	/**
	 * 执行秒杀
	 */
	public SeckillStateEnum executeSeckill(long seckillId, String phone){
		
		Jedis jedis = jedisPool.getResource();
		
		String StockStr = jedis.get(RedisKey.SECKILL_STOCK + seckillId);
		
		int skillStock = Integer.valueOf(StockStr);
		
		if (skillStock <= 0) {
			jedis.close();
			logger.info("SECKILLSOLD_OUT. seckillId={},userPhone={}", seckillId, phone);
			return SeckillStateEnum.SECKILL_OUT;
		}
		
		if (jedis.sismember(RedisKey.SECKILL_USERS + seckillId, String.valueOf(phone))) {
			jedis.close();
			// 重复秒杀
			logger.info("SECKILL_REPEATED. seckillId={},userPhone={}", seckillId, phone);
			return SeckillStateEnum.REPEAT_KILL;
		} else {
			jedis.close();
			// 进入待秒杀队列，进行后续串行操作
			logger.info("ENQUEUE_PRE_SECKILL. seckillId={},userPhone={}", seckillId, phone);
			
			mQProducer.send(MQConfig.SECKILL_QUEUE, seckillId+","+phone);

			return SeckillStateEnum.ENQUEUE_PRE_SECKILL;
		}
	}


	/**
	 * 在Redis中真正进行秒杀操作
	 * 
	 */
	@Override
	public void handleInRedis(long seckillId, String phone) throws SeckillException {
		Jedis jedis = jedisPool.getResource();

		String inventoryKey = RedisKey.SECKILL_STOCK + seckillId;
		String boughtKey = RedisKey.SECKILL_USERS + seckillId;

		String inventoryStr = jedis.get(inventoryKey);
		int inventory = Integer.valueOf(inventoryStr);
		if (inventory <= 0) {
			logger.info("handleInRedis SECKILLSOLD_OUT. seckillId={},userPhone={}", seckillId, phone);
			throw new SeckillException(SeckillStateEnum.SECKILL_OUT);
		}
		if (jedis.sismember(boughtKey, phone)) {
			logger.info("handleInRedis SECKILL_REPEATED. seckillId={},userPhone={}", seckillId, phone);
			throw new SeckillException(SeckillStateEnum.REPEAT_KILL);
		}
		jedis.decr(inventoryKey);
		jedis.sadd(boughtKey, String.valueOf(phone));
		logger.info("handleInRedis_done--->"+phone);
	}

	
	 /**
    *
    * @param seckillId
    * @param userPhone
    * @return 0： 排队中; 1: 秒杀成功; 2： 秒杀失败
    */
   @Override
   public SeckillStateEnum isGrab(long seckill_id, String phone) {
       int result = 0 ;

       Jedis jedis = jedisPool.getResource();
       try {
           String boughtKey = RedisKey.SECKILL_USERS + seckill_id;
           if(jedis.sismember(boughtKey,phone)){
        	   return SeckillStateEnum.SECKILL_SUCCESS;
           };
       } catch (Exception ex) {
           logger.error(ex.getMessage(), ex);
           result = 0;
       }
       
       return SeckillStateEnum.ENQUEUE_PRE_SECKILL;
   }
	
	
	
	
	
	/**
	 * 先插入秒杀记录再减库存
	 */
//	@Override
//	@Transactional
//	public SeckillExecution updateInventory(long seckillId, String phone) throws SeckillException {
//		// 执行秒杀逻辑:减库存 + 记录购买行为
//		Date nowTime = new Date();
//		try {
//			// 插入秒杀记录(记录购买行为)
//			// 这处， seckill_record的id等于这个特定id的行被启用了行锁, 但是其他的事务可以insert另外一行，
//			// 不会阻止其他事务里对这个表的insert操作
//			int insertCount = payOrderDAO.insertPayOrder(seckillId, phone, nowTime);
//			// 唯一:seckillId,userPhone
//			if (insertCount <= 0) {
//				// 重复秒杀
//				logger.info("seckill REPEATED. seckillId={},userPhone={}", seckillId, phone);
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
//								phone);
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
