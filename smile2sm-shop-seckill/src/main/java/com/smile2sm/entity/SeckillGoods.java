package com.smile2sm.entity;

import java.util.Date;

public class SeckillGoods extends Goods{
	
	private int seckill_id;
	private int seckill_num;
	private double seckill_price;
	private Date seckill_start_time;
	private Date seckill_end_time;
	
	public int getSeckill_id() {
		return seckill_id;
	}
	public void setSeckill_id(int seckill_id) {
		this.seckill_id = seckill_id;
	}
	public int getSeckill_num() {
		return seckill_num;
	}
	public void setSeckill_num(int seckill_num) {
		this.seckill_num = seckill_num;
	}
	public double getSeckill_price() {
		return seckill_price;
	}
	public void setSeckill_price(double seckill_price) {
		this.seckill_price = seckill_price;
	}
	public Date getSeckill_start_time() {
		return seckill_start_time;
	}
	public void setSeckill_start_time(Date seckill_start_time) {
		this.seckill_start_time = seckill_start_time;
	}
	public Date getSeckill_end_time() {
		return seckill_end_time;
	}
	public void setSeckill_end_time(Date seckill_end_time) {
		this.seckill_end_time = seckill_end_time;
	}
}
