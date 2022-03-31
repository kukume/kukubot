package me.kuku.yuq.logic

import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.MiHoYoEntity
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.utils.*
import java.util.*

object MiHoYoLogic {

    private const val version = "2.3.0"

    fun login(account: String, password: String): Result<MiHoYoEntity> {
        val beforeJsonObject = OkHttpUtils.getJson("https://webapi.account.mihoyo.com/Api/create_mmt?scene_type=1&now=${System.currentTimeMillis()}&reason=bbs.mihoyo.com")
        val dataJsonObject = beforeJsonObject.getJSONObject("data").getJSONObject("mmt_data")
        val challenge = dataJsonObject.getString("challenge")
        val gt = dataJsonObject.getString("gt")
        val mmtKey = dataJsonObject.getString("mmt_key")
        val jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/tool/geetest",
            mapOf("challenge" to challenge, "gt" to gt, "referer" to "https://bbs.mihoyo.com/ys/"))
        if (jsonObject.getInteger("code") != 200) return Result.failure("验证码识别失败，请重试")
        val cha = jsonObject.getString("challenge")
        val validate = jsonObject.getString("validate")
        val rsaKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDvekdPMHN3AYhm/vktJT+YJr7cI5DcsNKqdsx5DZX0gDuWFuIjzdwButrIYPNmRJ1G8ybDIF7oDW2eEpm5sMbL9zs9ExXCdvqrn51qELbqj0XxtMTIpaCHFSI50PfPpTFV9Xt/hmyVwokoOXFlAEgCn+QCgGs52bFoYMtyi+xEQIDAQAB"
        val enPassword = password.rsaEncrypt(rsaKey)
        val map = mapOf("is_bh2" to "false", "account" to account, "password" to enPassword,
            "mmt_key" to mmtKey, "is_crypto" to "true", "geetest_challenge" to cha, "geetest_validate" to validate,
            "geetest_seccode" to "${validate}|jordan")
        val response = OkHttpUtils.post("https://webapi.account.mihoyo.com/Api/login_by_password", map, OkUtils.ua(UA.PC))
        val loginJsonObject = OkUtils.json(response)
        val infoDataJsonObject = loginJsonObject.getJSONObject("data")
        if (infoDataJsonObject.getInteger("status") != 1) return Result.failure(infoDataJsonObject.getString("msg"))
        var cookie = OkUtils.cookie(response)
        val infoJsonObject = infoDataJsonObject.getJSONObject("account_info")
        val accountId = infoJsonObject.getString("account_id")
        val ticket = infoJsonObject.getString("weblogin_token")
        val cookieJsonObject = OkHttpUtils.getJson("https://webapi.account.mihoyo.com/Api/cookie_accountinfo_by_loginticket?login_ticket=$ticket&t=${System.currentTimeMillis()}",
            OkUtils.headers(cookie, "", UA.PC))
        val cookieToken = cookieJsonObject.getJSONObject("data").getJSONObject("cookie_info").getString("cookie_token")
        cookie += "cookie_token=$cookieToken; account_id=$accountId; "
        val loginResponse = OkHttpUtils.post("https://bbs-api.mihoyo.com/user/wapi/login",
            OkUtils.json("{\"gids\":\"2\"}"), OkUtils.cookie(cookie)).also { it.close() }
        val finaCookie = OkUtils.cookie(loginResponse)
        cookie += finaCookie
        return Result.success(MiHoYoEntity().also { it.cookie = cookieToken })
    }

    private fun ds(n: String = "h8w582wxwgqvahcdkpvdhbh2w9casgfl"): String {
        val i = System.currentTimeMillis() / 1000
        val r = MyUtils.randomLetter(6)
        val c = MD5Utils.toMD5("salt=$n&t=$i&r=$r")
        return "$i,$r,$c"
    }

    private fun headerMap(miHoYoEntity: MiHoYoEntity): Map<String, String> {
        return mapOf("DS" to ds(), "x-rpc-app_version" to version, "x-rpc-client_type" to "5",
            "x-rpc-device_id" to UUID.randomUUID().toString(), "user-agent" to "Mozilla/5.0 (Linux; Android 10; V1914A Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/88.0.4324.181 Mobile Safari/537.36 miHoYoBBS/2.5.1",
            "Referer" to "https://webstatic.mihoyo.com/bbs/event/signin-ys/index.html?bbs_auth_required=true&act_id=e202009291139501&utm_source=bbs&utm_medium=mys&utm_campaign=icon",
            "cookie" to miHoYoEntity.cookie)
    }

    fun sign(miHoYoEntity: MiHoYoEntity): Result<Void> {
        val ssJsonObject = OkHttpUtils.getJson("https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz=hk4e_cn",
            OkUtils.cookie(miHoYoEntity.cookie))
        if (ssJsonObject.getInteger("retcode") != 0) return Result.failure(ssJsonObject.getString("message"))
        val jsonArray = ssJsonObject.getJSONObject("data").getJSONArray("list")
        if (jsonArray.size == 0) return Result.failure("您还没有原神角色！！")
        var jsonObject: JSONObject? = null
        for (obj in jsonArray) {
            val singleJsonObject = obj as JSONObject
            jsonObject = OkHttpUtils.postJson("https://api-takumi.mihoyo.com/event/bbs_sign_reward/sign",
                OkUtils.json("{\"act_id\":\"e202009291139501\",\"region\":\"cn_gf01\",\"uid\":\"${singleJsonObject.getString("game_uid")}\"}"),
                OkHttpUtils.addHeaders(headerMap(miHoYoEntity)))
        }
        val code = jsonObject?.getInteger("retcode")
        return if (code == 0) Result.success("签到成功！！", null)
        else if (code == -5003) Result.success("今日已签到！！", null)
        else Result.failure(jsonObject?.getString("message"))
    }

}