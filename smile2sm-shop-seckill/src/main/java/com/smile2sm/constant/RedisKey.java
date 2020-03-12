package com.smile2sm.constant;

/**
 * redis 常量
 *
 */
public class RedisKey {

	/** seckill_id Set 集合 */
	public static final String SECKILL_IDSET = "SECKILL_IDSET:";
	/** seckill_id 保存每个ID对应的seckillGoods 对应的信息 */
	public static final String SECKILL_ID = "SECKILL_ID:";
	/** seckill_id 保存每个ID对应的库存*/
	public static final String SECKILL_STOCK = "SECKILL_STOCK:";
	/** 秒杀到用户*/
	public static final String SECKILL_USERS = "SECKILL_USERS:";
}
