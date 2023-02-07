package me.kuku.mirai.logic

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.call.*
import io.ktor.client.request.*
import me.kuku.mirai.entity.AliDriverEntity
import me.kuku.pojo.CommonResult
import me.kuku.utils.client
import me.kuku.utils.setJsonBody

object AliDriverLogic {

    private val cache = mutableMapOf<String, AliDriverAccessToken>()

    suspend fun login1() = client.get("https://api.kukuqaq.com/alidrive/qrcode").body<AliDriverQrcode>()

    suspend fun login2(qrcode: AliDriverQrcode): CommonResult<AliDriverEntity> {
        val jsonNode = client.post("https://api.kukuqaq.com/alidrive/qrcode") {
            setJsonBody(qrcode)
        }.body<JsonNode>()
        return if (jsonNode.has("code")) {
            CommonResult.fail(message = jsonNode["message"].asText(), code = jsonNode["code"].asInt())
        } else CommonResult.success(AliDriverEntity().also {
            it.refreshToken = jsonNode["refreshToken"].asText()
        })
    }

    private suspend fun accessToken(aliDriverEntity: AliDriverEntity): String {
        val accessToken = cache[aliDriverEntity.refreshToken]
        return if (accessToken == null || accessToken.isExpire()) {
            val jsonNode = client.post("https://auth.aliyundrive.com/v2/account/token") {
                setJsonBody("""{"refresh_token": "${aliDriverEntity.refreshToken}", "grant_type": "refresh_token"}"}""")
            }.body<JsonNode>()
            if (jsonNode.has("code")) error(jsonNode["messsage"].asText())
            val token = "${jsonNode["token_type"].asText()} ${jsonNode["access_token"].asText()}"
            cache[aliDriverEntity.refreshToken] = AliDriverAccessToken(token, System.currentTimeMillis() + jsonNode["expires_in"].asLong() * 1000 * 60)
            token
        } else accessToken.accessToken
    }

    suspend fun sign(aliDriverEntity: AliDriverEntity): String {
        val accessToken = accessToken(aliDriverEntity)
        val jsonNode = client.post("https://member.aliyundrive.com/v1/activity/sign_in_list") {
            setJsonBody("{}")
            headers {
                append("Authorization", accessToken)
            }
        }.body<JsonNode>()
        return if (jsonNode["success"]?.asBoolean() == true) {
            "签到成功，本月已签到${jsonNode["result"]["signInCount"].asInt()}天"
        } else error(jsonNode["code"].asText())
    }

}


data class AliDriverQrcode(
    var qrcodeUrl: String = "",
    var ck: String = "",
    var csrfToken: String = "",
    var idToken: String = "",
    var hs: String = "",
    var t: Long = 0
)

data class AliDriverAccessToken(val accessToken: String, val expire: Long) {
    fun isExpire() = expire > System.currentTimeMillis()
}
