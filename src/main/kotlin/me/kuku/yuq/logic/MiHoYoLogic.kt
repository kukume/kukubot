package me.kuku.yuq.logic

import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.MiHoYoEntity
import me.kuku.pojo.Result
import me.kuku.utils.MD5Utils
import me.kuku.utils.MyUtils
import me.kuku.utils.OkHttpUtils
import me.kuku.utils.OkUtils
import java.util.*

object MiHoYoLogic {

    private const val version = "2.3.0"

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