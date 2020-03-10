package com.smile2sm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 使用令牌桶实现限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExtRateLimiter {
	
	/**
	 * 速率
	 */
	double permitsPerSecond();
	
	/**
	 * 获取令牌超时时间
	 */
	long timeout();
}
