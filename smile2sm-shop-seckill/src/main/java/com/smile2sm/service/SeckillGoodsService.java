package com.smile2sm.service;

import java.util.List;

import com.smile2sm.entity.SeckillGoods;

public interface SeckillGoodsService {

	List<SeckillGoods> listSeckillGoods();
	
	SeckillGoods getSeckillGoodsDetail(long goods_id);
	
	SeckillGoods getSeckillUrl(long goods_id);
}
