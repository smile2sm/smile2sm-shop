package com.smile2sm.exception;

import com.smile2sm.enums.SeckillStateEnum;

/**
 * 秒杀相关业务异常
 */
@SuppressWarnings("serial")
public class SeckillException extends RuntimeException {

    private SeckillStateEnum seckillStateEnum;

    public SeckillException(SeckillStateEnum seckillStateEnum) {
        this.seckillStateEnum = seckillStateEnum;
    }

    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }

    public SeckillStateEnum getSeckillStateEnum() {
        return seckillStateEnum;
    }

    public void setSeckillStateEnum(SeckillStateEnum seckillStateEnum) {
        this.seckillStateEnum = seckillStateEnum;
    }
}
