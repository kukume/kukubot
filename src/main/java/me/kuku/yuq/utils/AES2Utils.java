package me.kuku.yuq.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

public class AES2Utils {

	private static final IvParameterSpec IV_PARAMETER_SPEC = new IvParameterSpec("0000000000000000".getBytes());

	/**
	 *  加密成十六进制字符串
	 *
	 *  <p>
	 *     使用AES加密，并将Cipher加密后的byte数组转换成16进制字符串
	 *  </p>
	 *
	 * @author Cr
	 * @date 2020-03-22
	 * */
	public static String encryptIntoHexString(String data, String key){
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"), IV_PARAMETER_SPEC);
			return bytesConvertHexString(cipher.doFinal(Arrays.copyOf(data.getBytes(), 16 * ((data.getBytes().length / 16) + 1))));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将加密后的十六进制字符串进行解密
	 *
	 * @author Cr
	 * @date 2020-03-22
	 *
	 * **/
	public static String decryptByHexString(String data, String key){
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"), IV_PARAMETER_SPEC);
			return new String(cipher.doFinal(hexStringConvertBytes(data.toLowerCase())),"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将加密后的十六进制字符串进行解密
	 *
	 * @author Cr
	 * @date 2020-03-22
	 *
	 * **/
	public static String decryptByHexString(String data, String key, byte[] iv){
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"), new IvParameterSpec(iv));
			return new String(cipher.doFinal(hexStringConvertBytes(data.toLowerCase())),"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 *  byte数组转换成十六进制字符串
	 *
	 *  <p>
	 *      先对每个byte数值补码成十进制,
	 *      然后在将十进制转换成对应的十六进制.
	 *      如果单次转换, 十六进制只有一位时， 将在前面追加0变成两位.
	 *  </p>
	 *
	 * @author Cr
	 * @date 2020-03-22
	 * */
	private static String bytesConvertHexString(byte [] data){
		StringBuffer result = new StringBuffer();
		String hexString = "";
		for (byte b : data) {
			// 补码成正十进制后转换成16进制
			hexString = Integer.toHexString(b & 255);
			result.append(hexString.length() == 1 ? "0" + hexString : hexString);
		}
		return result.toString().toUpperCase();
	}

	/**
	 * 十六进制字符串转换成byte数组
	 *
	 *  <p>
	 *      在加密时, 十六进制数值和byte字节的对应关系 是:  2个十六进制数值对应  1个byte字节  (2: 1)
	 *      所以byte数组的长度应该是十六进制字符串的一半, 并且在转换时
	 *      应是两个十六进制数值转换成一个byte字节  (2个2个十六进制数值进行转换)
	 *     这也是为什么可以*2的原因， 例如: 0, 2, 4, 6, 8, 10, 12 依次遍历
	 *  </p>
	 *
	 * @author Cr
	 * @date 2020-04-22
	 * */
	private static byte [] hexStringConvertBytes(String data){
		int length = data.length() / 2;
		byte [] result = new byte[length];
		for (int i = 0; i < length; i++) {
			int first = Integer.parseInt(data.substring(i * 2, i * 2 + 1), 16);
			int second = Integer.parseInt(data.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (first * 16 + second);
		}
		return result;
	}

}
