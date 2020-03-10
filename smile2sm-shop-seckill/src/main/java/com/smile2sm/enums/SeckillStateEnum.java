package com.smile2sm.enums;

/**
 * 枚举表述常量数据
 */
public enum SeckillStateEnum {
	
    ENQUEUE_PRE_SECKILL(6, "排队中..."),
    /**
     * 释放分布式锁失败，抢购失败
     */
    DISTLOCK_RELEASE_FAILED(5, "没抢到"),
    /**
     * 获取分布式锁失败，抢购失败
     */
    DISTLOCK_ACQUIRE_FAILED(4, "没抢到"),

    /**
     * Redis秒杀没抢到
     */
    REDIS_ERROR(3, "没抢到"),
    SOLD_OUT(2, "已售罄"),
    SUCCESS(1, "抢购成功"),
    END(0, "抢购已结束"),
    REPEAT_KILL(-1, "重复抢购"),
    /**
     * 运行时才能检测到的所有异常-系统异常
     */
    INNER_ERROR(-2, "没抢到"),
    /**
     * md5错误的数据篡改
     */
    DATA_REWRITE(-3, "数据篡改"),

    DB_CONCURRENCY_ERROR(-4, "没抢到"),
    /**
     * 被AccessLimitService限流了
     */
    ACCESS_LIMIT(-5, "没抢到");


    private int state;
    private String stateInfo;

    SeckillStateEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public static SeckillStateEnum stateOf(int index) {
        for (SeckillStateEnum state : values()) {
            if (state.getState() == index) {
                return state;
            }
        }
        return null;
    }
}
