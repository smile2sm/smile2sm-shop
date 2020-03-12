package com.smile2sm.service;

import java.util.List;

import com.smile2sm.dto.SeckillExposer;
import com.smile2sm.entity.SeckillGoods;
import com.smile2sm.enums.SeckillStateEnum;
import com.smile2sm.exception.SeckillException;

public interface SeckillGoodsService {

	List<SeckillGoods> listSeckillGoods();
	
	SeckillGoods getSeckillGoodsDetail(long goods_id);
	
	SeckillStateEnum executeSeckill(long seckill_id,String phone,String md5) throws SeckillException;

	void handleInRedis(long seckillId, String phone) throws SeckillException;

	SeckillExposer exposer(long seckill_id);

	SeckillStateEnum isGrab(long seckill_id, String phone);

	void updateStock(long seckill_id, String phone) throws SeckillException;

}
