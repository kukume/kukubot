package me.kuku.yuq.logic.impl

import com.IceCreamQAQ.Yu.annotation.Config
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.logic.QQAILogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.MD5Utils
import me.kuku.yuq.utils.OkHttpClientUtils
import okhttp3.FormBody
import java.net.URLEncoder
import java.util.*

class QQAILogicImpl: QQAILogic {
    @Config("YuQ.Mirai.bot.ai.appId")
    private lateinit var appId: String
    @Config("YuQ.Mirai.bot.ai.appKey")
    private lateinit var appKey: String

    private fun getSign(map: Map<String, String>): String{
        val treeMap = TreeMap<String, String>()
        treeMap.putAll(map)
        val sb = StringBuilder()
        for ((k, v) in treeMap){
            sb.append("$k=${URLEncoder.encode(v, "utf-8")}&")
        }
        sb.append("app_key=$appKey")
        return MD5Utils.toMD5(sb.toString()).toUpperCase()
    }

    private fun addParams(otherParams: Map<String, String>): FormBody {
        val map = mutableMapOf("app_id" to appId, "time_stamp" to (Date().time / 1000).toString(), "nonce_str" to BotUtils.randomStr(16))
        val builder = FormBody.Builder()
        map.putAll(otherParams)
        val sign = this.getSign(map)
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

    override fun pornIdentification(imageUrl: String): Boolean{
        val baseStr = this.imageUrlToBase64(imageUrl)
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/vision/vision_porn",
                addParams(mapOf("image" to baseStr)))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ret") == 0){
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("tag_list")
            /*val normal = jsonArray.getJSONObject(0).getInteger("tag_confidence")
            val hot = jsonArray.getJSONObject(1).getInteger("tag_confidence")*/
            val porn = jsonArray.getJSONObject(2).getInteger("tag_confidence")
            porn > 83/* || hot > normal*/
        }else false
    }

    override fun generalOCR(imageUrl: String){
        val baseStr = this.imageUrlToBase64(imageUrl)
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/ocr/ocr_generalocr",
                addParams(mapOf("image" to baseStr)))
        val jsonObject = OkHttpClientUtils.getJson(response)
        println(jsonObject)
    }

    override fun generalOCRToCaptcha(byteArray: ByteArray): CommonResult<String>{
        val b64 = Base64.getEncoder().encodeToString(byteArray)
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/ocr/ocr_generalocr",
                addParams(mapOf("image" to b64)))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ret") == 0){
            val itemJsonArray = jsonObject.getJSONObject("data").getJSONArray("item_list")
            if (itemJsonArray.size < 1) return CommonResult(200, "", "")
            val jsonArray = itemJsonArray.getJSONObject(0).getJSONArray("words")
            var code = ""
            jsonArray.forEach {
                val wordJsonObject = it as JSONObject
                val cha = wordJsonObject.getString("character")
                if (cha != "" &&( cha[0].isLetter() || cha[0].isDigit())) code += cha
            }
            CommonResult(200, "", code)
        }else CommonResult(500, jsonObject.getString("msg"))
    }

    override fun textChat(question: String, session: String): String{
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/nlp/nlp_textchat",
                addParams(mapOf("session" to session, "question" to question)))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ret") == 0)
            jsonObject.getJSONObject("data").getString("answer")
        else jsonObject.getString("msg")
    }
}