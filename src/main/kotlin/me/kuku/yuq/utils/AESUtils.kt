package me.kuku.yuq.utils

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.HashMap
import kotlin.experimental.and

object AESUtils {
    //认证密钥(自行随机生成)
    private const val AK = "s2ip9g3y3bjr5zz7ws6kjgx3ysr82zzw" //AccessKey

    private const val SK = "uv8zr0uen7aim8m7umcuooqzdv8cbvtf" //SecretKey


    //加密
    fun encrypt(content: String, key: String, iv: String): String {
        val raw = key.toByteArray(charset("utf-8"))
        val skeySpec = SecretKeySpec(raw, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") //"算法/模式/补码方式"
        //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        val ips = IvParameterSpec(iv.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ips)
        val encrypted = cipher.doFinal(content.toByteArray())
        return Base64.getEncoder().encodeToString(encrypted)
    }

    //解密
    fun decrypt(content: String?, key: String, iv: String): String? {
        return try {
            val raw = key.toByteArray(charset("utf-8"))
            val skeySpec = SecretKeySpec(raw, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val ips = IvParameterSpec(iv.toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ips)
            val encrypted1 = Base64.getDecoder().decode(content)
            try {
                val original = cipher.doFinal(encrypted1)
                String(original)
            } catch (e: Exception) {
                println(e.toString())
                null
            }
        } catch (ex: Exception) {
            println(ex.toString())
            null
        }
    }

    //获取认证签名(身份认证需要)
    fun getSign(currentTime: String): String? {
        var sign = ""
        val map: MutableMap<String, Any> = HashMap()
        map["ak"] = AK
        map["sk"] = SK
        map["ts"] = currentTime
        //获取 参数字典排序后字符串
        val decrypt = getOrderMap(map)
        try {
            //指定sha1算法
            val digest = MessageDigest.getInstance("SHA-1")
            digest.update(decrypt.toByteArray())
            //获取字节数组
            val messageDigest = digest.digest()
            // Create Hex String
            val hexString = StringBuffer()
            // 字节数组转换为十六进制数
            for (i in messageDigest.indices) {
                val shaHex = Integer.toHexString((messageDigest[i] and 0xFF.toByte()).toInt())
                if (shaHex.length < 2) {
                    hexString.append(0)
                }
                hexString.append(shaHex)
            }
            sign = hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return sign
    }

    //获取参数的字典排序
    private fun getOrderMap(maps: Map<String, Any>): String {
        val paramNames: MutableList<String> = ArrayList()
        for ((_, value) in maps) {
            paramNames.add(value.toString())
        }
        paramNames.sort()
        val paramStr = StringBuilder()
        for (paramName in paramNames) {
            paramStr.append(paramName)
        }
        return paramStr.toString()
    }
}