package com.smile2sm.service;

import java.util.List;

import com.smile2sm.dto.SeckillState;
import com.smile2sm.entity.SeckillGoods;
import com.smile2sm.exception.SeckillException;
import com.smile2sm.service.impl.SeckillExecution;

public interface SeckillGoodsService {

	List<SeckillGoods> listSeckillGoods();
	
	SeckillGoods getSeckillGoodsDetail(long goods_id);
	
	SeckillGoods getSeckillUrl(long goods_id);
	
	SeckillState executeSeckill(long seckillId,String phone) throws SeckillException;


	void handleInRedis(long seckillId, String phone) throws SeckillException;

	SeckillExecution updateInventory(long seckillId, String phone) throws SeckillException;
}
