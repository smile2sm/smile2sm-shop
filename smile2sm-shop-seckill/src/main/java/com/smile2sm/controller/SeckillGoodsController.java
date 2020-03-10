package com.smile2sm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smile2sm.annotation.ExtRateLimiter;
import com.smile2sm.entity.SeckillGoods;
import com.smile2sm.service.SeckillGoodsService;

@Controller
public class SeckillGoodsController {

	@Autowired
	SeckillGoodsService seckillGoodsService;
	
	/**
	 * 抢购商品列表
	 */
	@RequestMapping("goodlist")
	public String getGoodList(Model model) {
		List<SeckillGoods> listSeckillGoods = seckillGoodsService.listSeckillGoods();
		model.addAttribute("goodsList", listSeckillGoods);
		return "html/goodsList";
	}
	
	@ExtRateLimiter(permitsPerSecond = 2,timeout = 2)
	@RequestMapping("goodDetail")
	public String goodDetail(long goods_id,Model model) {
		SeckillGoods seckillGoods= seckillGoodsService.getSeckillGoodsDetail(goods_id);
		model.addAttribute("seckillGoods", seckillGoods);
		return "html/goodsDetail";
	}
	
	@RequestMapping("getSeckillUrl")
	public String getSeckillUrl(long goods_id) {
		SeckillGoods seckillGoods= seckillGoodsService.getSeckillGoodsDetail(goods_id);
		return "html/goodsDetail";
	}
	
}
