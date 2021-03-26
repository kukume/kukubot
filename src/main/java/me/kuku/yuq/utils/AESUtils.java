package me.kuku.yuq.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AESUtils {
    public static String encrypt(String content, String key, String iv){
        try {
            byte[] raw = key.getBytes();
            SecretKeySpec spec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, spec, ips);
            byte[] encrypted = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        }catch (Exception e){
            return null;
        }
    }


    public static byte[] decryptLoc(byte[] aseKey, byte[] iv, byte[] data) throws Exception {
        Cipher cipher= Cipher.getInstance("AES/CBC/NoPadding");
        SecretKey secretKey= new SecretKeySpec(aseKey,"AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    private static final int AES_KEY_SIZE_128 = 128;
    private static final int AES_KEY_SIZE_192 = 192;
    private static final int AES_KEY_SIZE_256 = 256;

    private static final String RNG_ALGORITHM = "SHA1PRNG";

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_GCM_ALGORITHM = "AES/GCM/PKCS5Padding";

    /**
     * 生成 AES 密钥
     *
     * @param keysize 密钥长度，参数合法值为 128 或 256
     * @return
     */
    public static SecretKey generateAESKey(int keysize) {
        try {
            // 校验密钥长度
            if (keysize != AES_KEY_SIZE_128 && keysize != AES_KEY_SIZE_192 && keysize != AES_KEY_SIZE_256) {
                keysize = AES_KEY_SIZE_128;
            }
            // 创建安全随机数生成器
            SecureRandom random = SecureRandom.getInstance(RNG_ALGORITHM);
            // 创建 AES 算法生成器
            KeyGenerator generator = KeyGenerator.getInstance(AES_ALGORITHM);
            // 初始化算法生成器
            generator.init(keysize, random);
            return generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES 加密
     *
     * @param aseKey 密钥
     * @param plain  加密原文
     * @return 密文
     */
    public static byte[] aesEncrypt(byte[] aseKey, byte[] plain) {
        try {
            SecretKey secretKey = new SecretKeySpec(aseKey, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(plain);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES 加密
     *
     * @param aseKey AES 密钥
     * @param plain  加密原文
     * @param nonce  随机值
     * @return 密文
     */
    public static byte[] aesEncrypt(byte[] aseKey, byte[] plain, byte[] nonce) {
        try {
            // Generate an AES key from the sha256 hash of the password
            SecretKey secretKeySpec = new SecretKeySpec(aseKey, AES_ALGORITHM);
            // 获取 AES 密码器
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec zeroIv = new GCMParameterSpec(128, nonce);
            // 初始化密码器（加密模型）
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, zeroIv);
            return cipher.doFinal(plain);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * AES 解密
     *
     * @param aseKey    AES 密钥
     * @param encrypted 解密密文
     * @return 原文
     */
    public static byte[] aesDecrypt(byte[] aseKey, byte[] encrypted) {
        try {
            SecretKey secretKey = new SecretKeySpec(aseKey, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES 解密
     *
     * @param aseKey    AES 密钥
     * @param encrypted 解密密文
     * @param nonce     随机值
     * @return 原文
     */
    public static byte[] aesDecrypt(byte[] aseKey, byte[] encrypted, byte[] nonce) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(aseKey, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec zeroIv = new GCMParameterSpec(128, nonce);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, zeroIv);
            return cipher.doFinal(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取随机值
     *
     * @param len 随机值长度
     * @return
     */
    public static byte[] generatorNonce(int len) {
        byte[] values = new byte[len];
        SecureRandom random = new SecureRandom();
        random.nextBytes(values);
        return values;
    }
}
