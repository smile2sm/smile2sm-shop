package com.smile2sm.aop;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.util.concurrent.RateLimiter;
import com.smile2sm.annotation.ExtRateLimiter;

@Aspect
@Component
public class RateLimiterAop {

	private static ConcurrentHashMap<String,RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
	@Pointcut("execution(* com.smile2sm.controller.*.*(..))")
	void pointcat() { }
	
	@Around("pointcat()")
	public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		//获取带有ExtRateLimiter注解的方法
		MethodSignature  methodSignature  = (MethodSignature) proceedingJoinPoint.getSignature();
		Method method = methodSignature.getMethod();
		ExtRateLimiter extRateLimiter = method.getDeclaredAnnotation(ExtRateLimiter.class);
		//没有注解，直接放行
		if(extRateLimiter == null ) {
			return proceedingJoinPoint.proceed();
		}
		
		double permitsPerSecond = extRateLimiter.permitsPerSecond();
		long timeout = extRateLimiter.timeout();
		//获取请求url
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		String requestURI = servletRequestAttributes.getRequest().getRequestURI();
		//根据请求地址获取令牌桶，不存在，则创建一个
		RateLimiter rateLimiter = rateLimiterMap.get(requestURI);
		if(rateLimiter == null) {
			rateLimiter = RateLimiter.create(permitsPerSecond);
			rateLimiterMap.put(requestURI, rateLimiter);
		}
		
		//尝试获取令牌
		boolean tryAcquire = rateLimiter.tryAcquire(timeout, TimeUnit.SECONDS);
		if(!tryAcquire) {
			//获取不到令牌，直接走服务降级
			HttpServletResponse response = servletRequestAttributes.getResponse();
			System.out.println("获取不到令牌！");
			
			PrintWriter writer = response.getWriter();
			writer.write("服务器忙，请稍后重试！");
			writer.flush();
			writer.close();
		}
		
		return proceedingJoinPoint.proceed();
	}
}
