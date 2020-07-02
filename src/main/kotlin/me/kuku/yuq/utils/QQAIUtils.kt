package me.kuku.yuq.utils

import okhttp3.FormBody
import java.net.URLEncoder
import java.util.*

class QQAIUtils {

    private val appId = ""
    private val appKey = ""

    private fun getSign(map: Map<String, String>, key: String): String{
        val treeMap = TreeMap<String, String>()
        treeMap.putAll(map)
        var str = ""
        for ((k, v) in treeMap){
            str += "$k=${URLEncoder.encode(v, "utf-8")}&"
        }
        str += "app_key=$key"
        return MD5Utils.toMD5(str).toUpperCase()
    }

    private fun addParams(otherParams: Map<String, String>): FormBody {
        val map = mutableMapOf("app_id" to appId, "time_stamp" to (Date().time / 1000).toString(), "nonce_str" to BotUtils.randomStr(16))
        val builder = FormBody.Builder()
        map.putAll(otherParams)
        val sign = this.getSign(map, appKey)
        map["sign"] = sign
        for ((k , v) in map){
            builder.add(k, v)
        }
        return builder.build()
    }

    private fun imageUrlToBase64(imageUrl: String): String{
        val response = OkHttpClientUtils.get(imageUrl)
        val bytes = OkHttpClientUtils.getBytes(response)
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun pornIdentification(imageUrl: String){
        val baseStr = this.imageUrlToBase64(imageUrl)
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/vision/vision_porn",
                addParams(mapOf("image" to baseStr)))
        val jsonObject = OkHttpClientUtils.getJson(response)
        println(jsonObject)
    }

    fun generalOCR(imageUrl: String){
        val baseStr = this.imageUrlToBase64(imageUrl)
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/ocr/ocr_generalocr",
                addParams(mapOf("image" to baseStr)))
        val jsonObject = OkHttpClientUtils.getJson(response)
        println(jsonObject)
    }



}