package com.smile2sm.dto;

import com.smile2sm.enums.SeckillStateEnum;

public class SeckillState {
	private int code;
	private String msg;

	public SeckillState(SeckillStateEnum seckillStateEnum) {
		this.code = seckillStateEnum.getCode();
		this.msg = seckillStateEnum.getMsg();
	}
	
	public SeckillState() {
		super();
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
}
