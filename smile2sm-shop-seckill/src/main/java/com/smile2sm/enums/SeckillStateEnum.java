package com.smile2sm.enums;

/**
 * 枚举表述常量数据
 */
public enum SeckillStateEnum{
	
    SECKILL_SUCCESS(0, "秒杀成功"),
    
    USER_LOGOUT(50000,"没有登录"),

    SECKILL_OUT(50001, "已售罄"),
    
    REDIS_ERROR(50002, "没秒杀到"),
    
    SECKILL_QUEUE(50003, "排队中..."),
    
    SECKILL_END(50004, "秒杀已结束"),
    
    SECKILL_REPEAT(50005, "重复秒杀"),
    
    ACCESS_LIMIT(50006, "没抢到"),
	
	RUNTIME_ERROR(50007, "没秒杀到"),
	
	ORDER_ERROR(50008, "不存在订单"),
	
	CREATE_ORDER_ERROR(50009, "创建订单失败"),
	
	MD5_ERROR(50010, "数据篡改");
	

    private int code;
    private String msg;
    
    SeckillStateEnum(int code, String msg) {
		this.code = code;
		this.msg = msg;
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
	
	public static SeckillStateEnum stateOf(int index) {
        for (SeckillStateEnum state : values()) {
            if (state.code == index) {
                return state;
            }
        }
        return null;
    }
}
