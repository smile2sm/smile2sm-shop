package com.smile2sm.dto;

import com.smile2sm.enums.SeckillStateEnum;

@SuppressWarnings("all")
public class Result<T> {

	private int code;
	private String msg;
	private T data;
	
	public static <T> Result setResult(SeckillStateEnum seckillStateEnum) {
		return new Result(seckillStateEnum.getCode(),seckillStateEnum.getMsg());
	}
	
	public static <T> Result success(T data) {
		return new Result(0,"success",data);
	}
	
	public static <T> Result error(int code, String msg) {
		return new Result(code,msg);
	}
	
	private Result(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	private Result(int code, String msg,T data) {
		this.code = code;
		this.msg = msg;
		this.data = data;
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
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}

}

