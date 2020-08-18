package me.kuku.yuq.utils

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

object RSAUtils {
    /**
     * RSA最大加密明文大小
     */
    private const val MAX_ENCRYPT_BLOCK = 117

    /**
     * RSA最大解密密文大小
     */
    private const val MAX_DECRYPT_BLOCK = 128

    /**
     * 获取密钥对
     *
     * @return 密钥对
     */
    fun getKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(1024)
        return generator.generateKeyPair()
    }

    /**
     * 获取私钥
     *
     * @param privateKey 私钥字符串
     * @return
     */
    fun getPrivateKey(privateKey: String): PrivateKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        val decodedKey = Base64.getDecoder().decode(privateKey.toByteArray())
        val keySpec = PKCS8EncodedKeySpec(decodedKey)
        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * 获取公钥
     *
     * @param publicKey 公钥字符串
     * @return
     */
    fun getPublicKey(publicKey: String): PublicKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        val decodedKey = Base64.getDecoder().decode(publicKey.toByteArray())
        val keySpec = X509EncodedKeySpec(decodedKey)
        return keyFactory.generatePublic(keySpec)
    }

    fun getPublicKey(modulus: String?, publicExponent: String?): PublicKey {
        val bigIntModulus = BigInteger(modulus, 16)
        val bigIntPrivateExponent = BigInteger(publicExponent, 16)
        val keySpec = RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * RSA加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return
     */
    fun encrypt(data: String, publicKey: PublicKey?): String {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val inputLen = data.toByteArray().size
        val out = ByteArrayOutputStream()
        var offset = 0
        var cache: ByteArray
        var i = 0
        // 对数据分段加密
        while (inputLen - offset > 0) {
            cache = if (inputLen - offset > MAX_ENCRYPT_BLOCK) {
                cipher.doFinal(data.toByteArray(), offset, MAX_ENCRYPT_BLOCK)
            } else {
                cipher.doFinal(data.toByteArray(), offset, inputLen - offset)
            }
            out.write(cache, 0, cache.size)
            i++
            offset = i * MAX_ENCRYPT_BLOCK
        }
        val encryptedData = out.toByteArray()
        out.close()
        // 获取加密内容使用base64进行编码,并以UTF-8为标准转化成字符串
        // 加密后的字符串
        return String(Base64.getEncoder().encode(encryptedData))
    }

    /**
     * RSA解密
     *
     * @param data       待解密数据
     * @param privateKey 私钥
     * @return
     */
    fun decrypt(data: String?, privateKey: PrivateKey?): String {
        val cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val dataBytes = Base64.getDecoder().decode(data)
        val inputLen = dataBytes.size
        val out = ByteArrayOutputStream()
        var offset = 0
        var cache: ByteArray
        var i = 0
        // 对数据分段解密
        while (inputLen - offset > 0) {
            cache = if (inputLen - offset > MAX_DECRYPT_BLOCK) {
                cipher.doFinal(dataBytes, offset, MAX_DECRYPT_BLOCK)
            } else {
                cipher.doFinal(dataBytes, offset, inputLen - offset)
            }
            out.write(cache, 0, cache.size)
            i++
            offset = i * MAX_DECRYPT_BLOCK
        }
        val decryptedData = out.toByteArray()
        out.close()
        // 解密后的内容
        return String(decryptedData, Charset.forName("utf-8"))
    }

    /**
     * 签名
     *
     * @param data       待签名数据
     * @param privateKey 私钥
     * @return 签名
     */
    fun sign(data: String, privateKey: PrivateKey): String {
        val keyBytes = privateKey.encoded
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val key = keyFactory.generatePrivate(keySpec)
        val signature = Signature.getInstance("MD5withRSA")
        signature.initSign(key)
        signature.update(data.toByteArray())
        return String(Base64.getEncoder().encode(signature.sign()))
    }

    /**
     * 验签
     *
     * @param srcData   原始字符串
     * @param publicKey 公钥
     * @param sign      签名
     * @return 是否验签通过
     */
    fun verify(srcData: String, publicKey: PublicKey, sign: String): Boolean {
        val keyBytes = publicKey.encoded
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val key = keyFactory.generatePublic(keySpec)
        val signature = Signature.getInstance("MD5withRSA")
        signature.initVerify(key)
        signature.update(srcData.toByteArray())
        return signature.verify(Base64.getDecoder().decode(sign.toByteArray()))
    }
}