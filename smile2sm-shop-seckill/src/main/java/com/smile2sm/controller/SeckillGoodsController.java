package com.smile2sm.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smile2sm.annotation.ExtRateLimiter;
import com.smile2sm.dto.Result;
import com.smile2sm.dto.SeckillExposer;
import com.smile2sm.entity.SeckillGoods;
import com.smile2sm.enums.SeckillStateEnum;
import com.smile2sm.exception.SeckillException;
import com.smile2sm.service.SeckillGoodsService;

@Controller
@RequestMapping("seckill/")
public class SeckillGoodsController {

	@Autowired
	SeckillGoodsService seckillGoodsService;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/time/now", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> time() {
        Date now = new Date();
        return Result.success(now.getTime());
    }
	
	/**
	 * 抢购商品列表
	 */
	@RequestMapping("goodList")
	public String getGoodList(Model model) {
		List<SeckillGoods> listSeckillGoods = seckillGoodsService.listSeckillGoods();
		model.addAttribute("goodList", listSeckillGoods);
		return "html/goodsList";
	}
	
	@RequestMapping("goodDetail/{seckill_id}")
	public String goodDetail(@PathVariable long seckill_id,Model model) {
		SeckillGoods seckillGoods= seckillGoodsService.getSeckillGoodsDetail(seckill_id);
		model.addAttribute("seckillGoods", seckillGoods);
		return "html/goodsDetail";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("exposer/{seckill_id}")
	@ResponseBody
	public Result<SeckillExposer> getSeckillUrl(@PathVariable long seckill_id) {
		SeckillExposer exposer = seckillGoodsService.exposer(seckill_id);
		return Result.success(exposer);
	}
	
	@SuppressWarnings("rawtypes")
	@ResponseBody
	@RequestMapping("executeSeckill/{seckill_id}/{phone}/{md5}")
	@ExtRateLimiter(permitsPerSecond = 2,timeout = 2)
	public Result executeSeckill(@PathVariable long seckill_id,@PathVariable String phone,@PathVariable String md5) {
		if("".equals(phone) ||phone == null) {
			return Result.setResult(SeckillStateEnum.USER_LOGOUT);
		}
		SeckillStateEnum seckillStateEnum = null;
		try {
			seckillStateEnum = seckillGoodsService.executeSeckill(seckill_id,phone,md5);
		}catch (SeckillException e1) {
			seckillStateEnum = e1.getSeckillStateEnum();
		}catch (Exception e2) {
			seckillStateEnum = SeckillStateEnum.RUNTIME_ERROR;
		}
		return Result.setResult(seckillStateEnum);
	}
	
	@SuppressWarnings("rawtypes")
	@ResponseBody
	@RequestMapping("isGrab/{seckill_id}/{phone}")
	public Result isGrab(@PathVariable long seckill_id,@PathVariable String phone) {
		SeckillStateEnum seckillStateEnum = seckillGoodsService.isGrab(seckill_id, phone);
		return Result.setResult(seckillStateEnum);
	}
}
