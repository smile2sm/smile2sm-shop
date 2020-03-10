package com.smile2sm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.smile2sm.dao.SeckillGoodsDao;
import com.smile2sm.entity.SeckillGoods;
import com.smile2sm.service.SeckillGoodsService;

@Component
public class SeckillGoodsServiceImpl implements SeckillGoodsService{

	@Autowired
	SeckillGoodsDao seckillGoodsDao;

	@Override
	public List<SeckillGoods> listSeckillGoods() {
		return seckillGoodsDao.listSeckillGoods();
	}

	@Override
	public SeckillGoods getSeckillGoodsDetail(long goods_id) {
		return seckillGoodsDao.getGoodsDetail(goods_id);
	}

	@Override
	public SeckillGoods getSeckillUrl(long goods_id) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
