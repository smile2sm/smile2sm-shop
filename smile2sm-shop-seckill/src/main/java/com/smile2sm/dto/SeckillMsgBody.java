package com.smile2sm.dto;

public class SeckillMsgBody {
	private long seckillId;
	private String phone;
	
	public SeckillMsgBody() {
	}

	public SeckillMsgBody(long seckillId, String phone) {
		super();
		this.seckillId = seckillId;
		this.phone = phone;
	}

	public long getSeckillId() {
		return seckillId;
	}

	public void setSeckillId(long seckillId) {
		this.seckillId = seckillId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}

