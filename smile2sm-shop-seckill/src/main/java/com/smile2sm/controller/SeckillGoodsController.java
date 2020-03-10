package com.smile2sm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smile2sm.annotation.ExtRateLimiter;
import com.smile2sm.dto.Result;
import com.smile2sm.dto.SeckillState;
import com.smile2sm.entity.SeckillGoods;
import com.smile2sm.enums.SeckillStateEnum;
import com.smile2sm.exception.SeckillException;
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
	@ExtRateLimiter(permitsPerSecond = 2,timeout = 2)
	@RequestMapping("execute")
	@ResponseBody
	public Result executeSeckill(long seckill_id,String phone) {
		if("".equals(phone) ||phone == null) {
			return Result.error(500000, "没有登录！");
		}
		try {
			SeckillState executeSeckill = seckillGoodsService.executeSeckill(seckill_id, phone);
			return Result.success(executeSeckill);
		}catch (SeckillException e1) {
			SeckillStateEnum seckillStateEnum = e1.getSeckillStateEnum();
			return Result.error(seckillStateEnum.getCode(), seckillStateEnum.getMsg());
		}catch (Exception e2) {
			SeckillStateEnum seckillStateEnum = SeckillStateEnum.RUNTIME_ERROR;
			return Result.error(seckillStateEnum.getCode(), seckillStateEnum.getMsg());
		}
	}
	
}
