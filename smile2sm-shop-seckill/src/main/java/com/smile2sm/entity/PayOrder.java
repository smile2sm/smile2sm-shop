package com.smile2sm.entity;

import java.util.Date;

public class PayOrder {

	private long seckill_id;
	private String phone;
	private int state;
	private Date create_time;
	public long getSeckill_id() {
		return seckill_id;
	}
	public void setSeckill_id(long seckill_id) {
		this.seckill_id = seckill_id;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Date getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
}
