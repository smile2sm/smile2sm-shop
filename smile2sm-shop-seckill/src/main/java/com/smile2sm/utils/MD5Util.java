package com.smile2sm.utils;

import org.springframework.util.DigestUtils;

public class MD5Util {
	private static String SALT = "$$$$smile2sm%%%";

	public static String getMd5(long seckillId) {
		String base = seckillId + "/" + SALT;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}

}
