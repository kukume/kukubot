package me.kuku.yuq.logic.impl

import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.logic.QQAILogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.service.ConfigService
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.MD5Utils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.removeSuffixLine
import okhttp3.FormBody
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

class QQAILogicImpl: QQAILogic {

    @Inject
    private lateinit var configService: ConfigService

    private fun getSign(map: Map<String, String>, appKey: String): String{
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
        val configEntity1 = configService.findByType("qqAIAppId")
        val configEntity2 = configService.findByType("qqAIAppKey")
        val appId = configEntity1?.content ?: ""
        val appKey = configEntity2?.content ?: ""
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

    private fun urlToBase64(imageUrl: String): String{
        val response = OkHttpClientUtils.get(imageUrl)
        val bytes = OkHttpClientUtils.getBytes(response)
        return Base64.getEncoder().encodeToString(bytes)
    }

    override fun pornIdentification(imageUrl: String): Boolean{
        val baseStr = this.urlToBase64(imageUrl)
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

    override fun generalOCR(imageUrl: String): String{
        val baseStr = this.urlToBase64(imageUrl)
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/ocr/ocr_generalocr",
                addParams(mapOf("image" to baseStr)))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ret") == 0){
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("item_list")
            if (jsonArray.isEmpty()) return "啥文字也没有识别到！！"
            val sb = StringBuilder()
            jsonArray.forEach {
                val singleJsonObject = it as JSONObject
                sb.appendLine(singleJsonObject.getString("itemstring"))
            }
            sb.removeSuffixLine().toString()
        }else jsonObject.getString("msg")
    }

    override fun textChat(question: String, session: String): String{
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/nlp/nlp_textchat",
                addParams(mapOf("session" to session, "question" to question)))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("ret")){
            0 -> jsonObject.getJSONObject("data").getString("answer")
            16385 -> "您还没有填入appid！！"
            16394 -> "没有发现匹配的答案！！"
            else -> jsonObject.getString("msg")
        }
    }

    override fun echoSpeechRecognition(url: String): String {
        val speechResponse = OkHttpClientUtils.get(url)
        val bytes = OkHttpClientUtils.getBytes(speechResponse)
        val b64Str = Base64.getEncoder().encodeToString(bytes)
        val response = OkHttpClientUtils.post(
                "https://api.ai.qq.com/fcgi-bin/aai/aai_asr",
                addParams(
                        mapOf(
                                "format" to "3",
                                "speech" to b64Str,
                                "rate" to "16000"
                        )
                )
        )
        val jsonObject = OkHttpClientUtils.getJson(response)
        println(jsonObject)
        return ""
    }

    override fun aiLabSpeechRecognition(url: String): String {
        val speechResponse = OkHttpClientUtils.get(url)
        val bytes = OkHttpClientUtils.getBytes(speechResponse)
        val b64Str = Base64.getEncoder().encodeToString(bytes)
        val response = OkHttpClientUtils.post(
                "https://api.ai.qq.com/fcgi-bin/aai/aai_asrs",
                addParams(
                        mapOf(
                                "format" to "3",
                                "rate" to "16000",
                                "seq" to "0",
                                "len" to bytes.size.toString(),
                                "end" to "1",
                                "speech_id" to "123213123",
                                "speech_chunk" to b64Str
                        )
                )
        )
        val jsonObject = OkHttpClientUtils.getJson(response)
        println(jsonObject)
        return ""
    }

    override fun voiceSynthesis(text: String): CommonResult<ByteArray> {
        val response = OkHttpClientUtils.post("https://api.ai.qq.com/fcgi-bin/aai/aai_tts", addParams(mapOf(
                "speaker" to "5",
                "format" to "2",
                "volume" to "0",
                "speed" to "100",
                "text" to text,
                "aht" to "0",
                "apc" to "58"
        )))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("ret") == 0) {
            val base64 = jsonObject.getJSONObject("data").getString("speech")
            CommonResult(200, "", Base64.getDecoder().decode(base64))
        }else CommonResult(500, jsonObject.getString("msg"))
    }
}