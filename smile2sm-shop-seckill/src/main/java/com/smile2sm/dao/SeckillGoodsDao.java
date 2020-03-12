package com.smile2sm.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.smile2sm.entity.SeckillGoods;

@Mapper
public interface SeckillGoodsDao {

	@Select("select sg.*,g.* from seckillgoods sg Left join goods g On sg.goods_id=g.goods_id")
	public List<SeckillGoods> listSeckillGoods();
	
	@Select("select sg.*,g.* from seckillgoods sg Left join goods g On sg.goods_id=g.goods_id"
			+ " Where sg.seckill_id=#{seckill_id}")
	public SeckillGoods getGoodsDetail(long seckill_id);
	
	
	@Select("Update seckill_goods Set seckill_num=seckill_num-1 Where seckill_id=#{seckill_id} and seckill_num>0")
	public int reduceSeckillNum(long seckill_id);
}
