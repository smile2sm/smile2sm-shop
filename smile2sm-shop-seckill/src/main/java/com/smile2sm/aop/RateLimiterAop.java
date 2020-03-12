package com.smile2sm.aop;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.smile2sm.annotation.ExtRateLimiter;
import com.smile2sm.dto.Result;
import com.smile2sm.enums.SeckillStateEnum;
/**
 * RateLimiter 
 * 令牌桶限流Aop切面
 */
@Aspect
@Component
public class RateLimiterAop {

	//令牌桶Map，线程安全
	private static ConcurrentHashMap<String,RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

	/**
	 * 切入点
	 */
	@Pointcut("execution(* com.smile2sm.controller.*.*(..))")
	void pointCat() { }
	
	/**
	 * 环绕切入
	 */
	@Around("pointCat()")
	public Object around(ProceedingJoinPoint jPoint) throws Throwable {
		//获取带有ExtRateLimiter注解的方法
		ExtRateLimiter extRateLimiter = getExtRateLimiter(jPoint);
		//没有注解，直接放行
		if(extRateLimiter == null ) {
			return jPoint.proceed();
		}
		//ExtRateLimiter参数
		double permitsPerSecond = extRateLimiter.permitsPerSecond();
		long timeout = extRateLimiter.timeout();
		
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		//获取令牌桶
		RateLimiter rateLimiter = getRrateLimiter(servletRequestAttributes, permitsPerSecond);
		//尝试获取令牌
		boolean tryAcquire = rateLimiter.tryAcquire(timeout, TimeUnit.SECONDS);
		//获取不到令牌
		if(!tryAcquire) {
			fallBack(servletRequestAttributes);
			return null;
		}
		
		return jPoint.proceed();
	}

	/**
	 * 获取不到令牌，直接走服务降级
	 */
	private void fallBack(ServletRequestAttributes servletRequestAttributes) throws IOException {
		HttpServletResponse response = servletRequestAttributes.getResponse();
		PrintWriter writer = response.getWriter();
		//服务降级提示
		SeckillStateEnum accessLimit = SeckillStateEnum.ACCESS_LIMIT;
		String resultStr = JSON.toJSONString(Result.setResult(accessLimit));
		writer.write(resultStr);
		writer.flush();
		writer.close();
	}
	
	/**
	 * 获取对应方法的ExtRateLimiter注解
	 */
	private ExtRateLimiter getExtRateLimiter(ProceedingJoinPoint jPoint) {
		MethodSignature  methodSignature  = (MethodSignature) jPoint.getSignature();
		Method method = methodSignature.getMethod();
		ExtRateLimiter extRateLimiter = method.getDeclaredAnnotation(ExtRateLimiter.class);
		return extRateLimiter;
	}
	
	/**
	 * 获取令牌桶,在ConcurrentHashMap中获取不到，则创建一个并存入ConcurrentHashMap中，key为
	 * 请求的url
	 */
	private RateLimiter getRrateLimiter(ServletRequestAttributes servletRequestAttributes,double permitsPerSecond) {
		//获取请求url
		String requestURI = servletRequestAttributes.getRequest().getRequestURI();
		//根据请求地址获取令牌桶，不存在，则创建一个
		RateLimiter rateLimiter = rateLimiterMap.get(requestURI);
		
		if(rateLimiter == null) {
			rateLimiter = RateLimiter.create(permitsPerSecond);
			rateLimiterMap.put(requestURI, rateLimiter);
		}		
		return rateLimiter;
	}
	
}
