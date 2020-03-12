package com.smile2sm.dao;

import java.util.Date;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.smile2sm.entity.PayOrder;

@Mapper
public interface PayOrderDao {

	@Insert("insert into pay_order(seckill_id,phone,state,create_time) Values(#{seckill_id},#{phone},#{state},#{create_time})")
	public int insertPayOrder(long seckill_id,String phone,int state,Date create_time);
	
	@Select("Select * From pay_order Where seckill_id=#{seckill_id} And phone=#{phone}")
	public PayOrder getPayOrder(long seckill_id,String phone);
	
}
