package me.kuku.yuq.logic.impl

import me.kuku.yuq.entity.SteamEntity
import me.kuku.yuq.logic.SteamLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.RSAUtils
import okhttp3.MultipartBody
import java.util.*

class SteamLogicImpl: SteamLogic {

    override fun login(username: String, password: String, twoCode: String): CommonResult<Map<String, String>> {
        val firstResponse = OkHttpClientUtils.post("https://steamcommunity.com/login/getrsakey/", OkHttpClientUtils.addForms(
                "donotcache", Date().time.toString(),
                "username", username
        ), OkHttpClientUtils.addUA(OkHttpClientUtils.PC_UA))
        var cookie = OkHttpClientUtils.getCookie(firstResponse)
        val jsonObject = OkHttpClientUtils.getJson(firstResponse)
        val mod = jsonObject.getString("publickey_mod")
        val exp = jsonObject.getString("publickey_exp")
        val publicKey = RSAUtils.getPublicKey(mod, exp)
        val encryptPassword = RSAUtils.encrypt(password, publicKey)
        val secondResponse = OkHttpClientUtils.post("https://steamcommunity.com/login/dologin/", OkHttpClientUtils.addForms(
                "donotcache", Date().time.toString(),
                "password", encryptPassword,
                "username", username,
                "twofactorcode", twoCode,
                "emailauth", "",
                "loginfriendlyname", "",
                "captchagid", "-1",
                "captcha_text", "",
                "emailsteamid", "",
                "rsatimestamp", jsonObject.getString("timestamp"),
                "remember_login", "true"
        ), OkHttpClientUtils.addHeaders("user-agent", OkHttpClientUtils.PC_UA, "cookie", cookie))
        val secondJsonObject = OkHttpClientUtils.getJson(secondResponse)
        val success = secondJsonObject.getBoolean("success")
        val twoVerify = secondJsonObject.getBoolean("requires_twofactor")
        return if (success && !twoVerify) {
            // steamid  webcookie  auth  token_secure  remember_login
            val infoJsonObject = secondJsonObject.getJSONObject("transfer_parameters")
            cookie += OkHttpClientUtils.getCookie(secondResponse)
            CommonResult(200, "成功", mapOf(
                    "cookie" to cookie, "steamId" to infoJsonObject.getString("steamid")
            ))
        }else if (!success && twoVerify) CommonResult(500, "二次验证码错误！")
        else CommonResult(500, secondJsonObject.getString("message"))
    }

    override fun changeName(steamEntity: SteamEntity, name: String): String {
        val randomStr = BotUtils.randomStr(24)
        val response = OkHttpClientUtils.post("https://steamcommunity.com/profiles/${steamEntity.steamId}/edit", OkHttpClientUtils.addForms(
                "sessionID", randomStr,
                "type", "profileSave",
                "weblink_1_title", "",
                "weblink_1_url", "",
                "weblink_2_title", "",
                "weblink_2_url", "",
                "weblink_3_title", "",
                "weblink_3_url", "",
                "personaName", name,
                "real_name", "",
                "country", "",
                "state", "",
                "city", "",
                "customURL", "",
                "summary", "未提供信息。",
                "favorite_badge_badgeid", "",
                "favorite_badge_communityitemid", "",
                "primary_group_steamid", "35272541"
        ), OkHttpClientUtils.addCookie(steamEntity.cookie + "sessionid=$randomStr; "))
        response.close()
        return "steam更名提交成功，若修改未生效即为cookie已失效"
    }

    override fun loginToBuff(steamEntity: SteamEntity): CommonResult<String> {
        val firstResponse = OkHttpClientUtils.get("https://buff.163.com/account/login/steam?back_url=/account/steam_bind/finish")
        firstResponse.close()
        val randomStr = BotUtils.randomStr(24)
        val refererUrl = firstResponse.header("Location")!!
        val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("action", "steam_openid_login")
                .addFormDataPart("openid.mode", "checkid_setup")
                .addFormDataPart("openidparams", "eyJvcGVuaWQubW9kZSI6ImNoZWNraWRfc2V0dXAiLCJvcGVuaWQubnMiOiJodHRwOlwvXC9zcGVjcy5vcGVuaWQubmV0XC9hdXRoXC8yLjAiLCJvcGVuaWQucmVhbG0iOiJodHRwczpcL1wvYnVmZi4xNjMuY29tXC8iLCJvcGVuaWQuc3JlZ19yZXF1aXJlZCI6Im5pY2tuYW1lLGVtYWlsLGZ1bGxuYW1lIiwib3BlbmlkLmFzc29jX2hhbmRsZSI6Ik5vbmUiLCJvcGVuaWQucmV0dXJuX3RvIjoiaHR0cHM6XC9cL2J1ZmYuMTYzLmNvbVwvYWNjb3VudFwvbG9naW5cL3N0ZWFtXC92ZXJpZmljYXRpb24/YmFja191cmw9JTJGYWNjb3VudCUyRnN0ZWFtX2JpbmQlMkZmaW5pc2giLCJvcGVuaWQubnNfc3JlZyI6Imh0dHA6XC9cL29wZW5pZC5uZXRcL2V4dGVuc2lvbnNcL3NyZWdcLzEuMSIsIm9wZW5pZC5pZGVudGl0eSI6Imh0dHA6XC9cL3NwZWNzLm9wZW5pZC5uZXRcL2F1dGhcLzIuMFwvaWRlbnRpZmllcl9zZWxlY3QiLCJvcGVuaWQuY2xhaW1lZF9pZCI6Imh0dHA6XC9cL3NwZWNzLm9wZW5pZC5uZXRcL2F1dGhcLzIuMFwvaWRlbnRpZmllcl9zZWxlY3QifQ==")
                .addFormDataPart("nonce", randomStr)
                .build()
        val secondResponse = OkHttpClientUtils.post("https://steamcommunity.com/openid/login", multipartBody, OkHttpClientUtils.addHeaders(
                "cookie", steamEntity.cookie + "sessionidSecureOpenIDNonce=$randomStr; ",
                "referer", refererUrl
        ))
        secondResponse.close()
        val resultUrl = secondResponse.header("Location")
        return if (resultUrl != null) {
            val thirdResponse = OkHttpClientUtils.get(resultUrl)
            thirdResponse.close()
            val cookie = OkHttpClientUtils.getCookie(thirdResponse)
            return CommonResult(200, "成功", cookie)
        }else CommonResult(500, "获取网易BUFF的cookie失败，请更新steam！")
    }
}