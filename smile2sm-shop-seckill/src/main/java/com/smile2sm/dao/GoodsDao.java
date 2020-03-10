package com.smile2sm.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.smile2sm.entity.Goods;

@Mapper
public interface GoodsDao {
	
	@Select("select g.* from goods g ")
	public List<Goods> listGoods();

	
}
