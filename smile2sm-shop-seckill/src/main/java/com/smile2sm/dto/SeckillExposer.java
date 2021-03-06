package com.smile2sm.dto;

public class SeckillExposer {

	private boolean exposed;
	private long seckill_id;
	private String md5;
	private long seckill_start_time;
	private long seckill_end_time;
	private long seckill_now_time;
	
	public SeckillExposer(boolean exposed, long seckill_id,long seckill_start_time, long seckill_end_time,
			long seckill_now_time) {
		super();
		this.exposed = exposed;
		this.seckill_id = seckill_id;
		this.seckill_start_time = seckill_start_time;
		this.seckill_end_time = seckill_end_time;
		this.seckill_now_time = seckill_now_time;
	}
	
	public SeckillExposer(boolean exposed,long seckill_id, String md5) {
		super();
		this.exposed = exposed;
		this.seckill_id = seckill_id;
		this.md5 = md5;
	}

	public SeckillExposer(boolean exposed, long seckill_id) {
		super();
		this.exposed = exposed;
		this.seckill_id = seckill_id;
	}
	public SeckillExposer() {
		super();
	}
	public boolean isExposed() {
		return exposed;
	}
	public void setExposed(boolean exposed) {
		this.exposed = exposed;
	}
	public long getSeckill_id() {
		return seckill_id;
	}
	public void setSeckill_id(long seckill_id) {
		this.seckill_id = seckill_id;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public long getSeckill_start_time() {
		return seckill_start_time;
	}
	public void setSeckill_start_time(long seckill_start_time) {
		this.seckill_start_time = seckill_start_time;
	}
	public long getSeckill_end_time() {
		return seckill_end_time;
	}
	public void setSeckill_end_time(long seckill_end_time) {
		this.seckill_end_time = seckill_end_time;
	}
	public long getSeckill_now_time() {
		return seckill_now_time;
	}
	public void setSeckill_now_time(long seckill_now_time) {
		this.seckill_now_time = seckill_now_time;
	}
}
