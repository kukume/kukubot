package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.pojo.Result
import me.kuku.utils.MyUtils
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.exception.BaiduAiException
import me.kuku.yuq.pojo.Token

@AutoBind
interface BaiduAiLogic {
    fun ocrGeneralBasic(baiduAiPojo: BaiduAiPojo, base: String): Result<String>
    fun antiPornImage(baiduAiPojo: BaiduAiPojo, base: String): Boolean
}

class BaiduAiLogicImpl: BaiduAiLogic{

    private val cache = mutableMapOf<String, Token>()

    private fun accessToken(apikey: String, secretKey: String): String{
        var token = cache[apikey]
        return if (token == null || token.isExpire()) {
            val jsonObject =
                OkHttpUtils.getJson("https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=$apikey&client_secret=$secretKey&")
            if (jsonObject.containsKey("error")) {
                throw BaiduAiException("${jsonObject.getString("error")}：${jsonObject.getString("error_description")}")
            } else {
                token = Token(jsonObject.getString("access_token"), jsonObject.getString("refresh_token"),
                    jsonObject.getLong("expires_in"))
                cache[apikey] = token
                token.accessToken
            }
        }else token.accessToken
    }

    override fun ocrGeneralBasic(baiduAiPojo: BaiduAiPojo, base: String): Result<String> {
        val jsonObject = OkHttpUtils.postJson("https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token=${accessToken(baiduAiPojo.ocrApiKey, baiduAiPojo.ocrSecretKey)}",
            mutableMapOf("image" to base))
        return if (jsonObject.containsKey("error_code")){
            throw BaiduAiException("${jsonObject.getString("error")}：${jsonObject.getString("error_description")}")
        }else {
            if (jsonObject.getInteger("words_result_num") == 0) Result.failure("没有识别到任何结果！！", null)
            else {
                val jsonArray = jsonObject.getJSONArray("words_result")
                val sb = StringBuilder()
                for (i in jsonArray.indices) {
                    sb.appendLine(jsonArray.getJSONObject(i).getString("words"))
                }
                Result.success(MyUtils.removeLastLine(sb))
            }
        }
    }

    override fun antiPornImage(baiduAiPojo: BaiduAiPojo, base: String): Boolean {
        val jsonObject = OkHttpUtils.postJson("https://aip.baidubce.com/rest/2.0/solution/v1/img_censor/v2/user_defined?access_token=${accessToken(baiduAiPojo.antiPornApiKey, baiduAiPojo.antiPornSecretKey)}",
            mutableMapOf("image" to base))
        return if (jsonObject.containsKey("err_code")) false
        else {
            if (jsonObject.getInteger("conclusionType") == 1) false
            else {
                val jsonArray = jsonObject.getJSONArray("data")
                if (!jsonArray.isEmpty()) {
                    for (i in jsonArray.indices){
                        val singleJsonObject = jsonArray.getJSONObject(i)
                        val type = singleJsonObject.getInteger("type")
                        if (type in arrayOf(0, 1, 2, 3, 4, 5, 7, 11, 12 ,13 , 15, 16, 21))
                            return true
                    }
                }
                false
            }
        }
    }
}

data class BaiduAiPojo(
    var ocrApiKey: String = "",
    var ocrSecretKey: String = "",
    var antiPornApiKey: String = "",
    var antiPornSecretKey: String = ""
)