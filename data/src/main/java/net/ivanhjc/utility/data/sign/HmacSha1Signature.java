/**
 * @Copyright (C) 2015 by ywx.co.,ltd.All Rights Reserved.
 *  YWX CONFIDENTIAL AND TRADE SECRET
 */
package net.ivanhjc.metanote.common.utils.sign;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
* @Title: HmacSha1Signature.java 
* @Description:  HMAC-SHA1 加密
* @author fangyi
* @date 2015年6月30日 下午2:57:04
 */
public class HmacSha1Signature {
	private static final String MAC_NAME = "HmacSHA1";  
    private static final String ENCODING = "UTF-8";  
    
	
    
	/**
	 * 使用 HMAC-SHA1 签名方法对对encryptText进行签名
	 * @param encryptText 被签名的字符串
	 * @param encryptKey 密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] sign(String encryptText, String encryptKey){
		if(encryptText == null || encryptKey == null){
			return null;
		}
		
		try {
			byte[] data = encryptKey.getBytes(ENCODING);
			SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
			Mac mac = Mac.getInstance(MAC_NAME);
			mac.init(secretKey);

			byte[] text = encryptText.getBytes(ENCODING);
			return mac.doFinal(text);
		} catch (Exception e) {
			throw new RuntimeException("HmacSha1Signature 加密出错", e);
		}
		
	}
}


