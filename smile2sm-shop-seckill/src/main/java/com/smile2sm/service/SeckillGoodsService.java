package com.smile2sm.service;

import java.util.List;

import com.smile2sm.dto.SeckillState;
import com.smile2sm.entity.SeckillExposer;
import com.smile2sm.entity.SeckillGoods;
import com.smile2sm.enums.SeckillStateEnum;
import com.smile2sm.exception.SeckillException;

public interface SeckillGoodsService {

	List<SeckillGoods> listSeckillGoods();
	
	SeckillGoods getSeckillGoodsDetail(long goods_id);
	
	SeckillStateEnum executeSeckill(long seckillId,String phone);

	void handleInRedis(long seckillId, String phone) throws SeckillException;

	SeckillExposer exposer(long seckill_id);

	SeckillStateEnum isGrab(long seckill_id, String phone);

	//SeckillExecution updateInventory(long seckillId, String phone) throws SeckillException;
}
