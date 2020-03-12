package com.smile2sm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
public class IndexController {
	@Autowired
	JedisPool jedisPool;
	
	@RequestMapping("index")
	public String indexController(Model model) {
		
		Jedis jedis = jedisPool.getResource();
		jedis.flushDB();
		jedis.close();
		return "html/index";
	}
	@RequestMapping("/")
	public String HomeController() {
		return "html/index";
	}
}
