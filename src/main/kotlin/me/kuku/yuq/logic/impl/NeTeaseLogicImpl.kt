package me.kuku.yuq.logic.impl

import com.IceCreamQAQ.Yu.toJSONString
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.NeTeaseEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.logic.NeTeaseLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.utils.AESUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.QQUtils
import kotlin.random.Random

class NeTeaseLogicImpl: NeTeaseLogic {

    private val referer = "http://music.163.com/"
    private val vi = "0102030405060708"
    private val nonce = "0CoJUm6Qyw8W8jud"
    private val secretKey = "TA3YiYCfY2dDJQgg"
    private val encSecKey = "84ca47bca10bad09a6b04c5c927ef077d9b9f1e37098aa3eac6ea70eb59df0aa28b691b7e75e4f1f9831754919ea784c8f74fbfadf2898b0be17849fd656060162857830e241aba44991601f137624094c114ea8d17bce815b0cd4e5b8e2fbaba978c6d1d14dc3d1faf852bdd28818031ccdaaa13a6018e1024e2aae98844210"

    private fun aesEncode(secretData: String, secret: String): String{
        return AESUtils.encrypt(secretData, secret, vi)
    }

    private fun prepare(map: Map<String, String>): Array<String> {
        var param = this.aesEncode(map.toJSONString(), nonce)
        param = this.aesEncode(param, secretKey)
        return arrayOf("params", param, "encSecKey", encSecKey)
    }

    /**
     * 密码需要md5
     */
    override fun loginByPhone(username: String, password: String): CommonResult<NeTeaseEntity> {
        val response = OkHttpClientUtils.post("https://music.163.com/weapi/login/cellphone", OkHttpClientUtils.addForms(
                *this.prepare(mapOf("phone" to username, "countrycode" to "86", "password" to password, "rememberLogin" to "true"))
        ), OkHttpClientUtils.addHeaders(
                "referer", referer,
                "user-agent", OkHttpClientUtils.PC_UA
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        val cookieMap = OkHttpClientUtils.getCookie(response, "MUSIC_U", "__csrf")
        return if (jsonObject.getInteger("code") == 200){
            CommonResult(200, "", NeTeaseEntity(null, 0L, username, password, cookieMap.getValue("MUSIC_U"), cookieMap.getValue("__csrf")))
        }else CommonResult(500, jsonObject.getString("msg"))
    }

    override fun loginByQQ(qqEntity: QQEntity): CommonResult<NeTeaseEntity> {
        val url = "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=100495085&daid=383&pt_skey_valid=1&style=35&s_url=http%3A%2F%2Fconnect.qq.com&refer_cgi=authorize&which=&auth_time=1594952756082&client_id=100495085&src=1&state=bxlowqcqQF&response_type=code&scope=&redirect_uri=https%3A%2F%2Fmusic.163.com%2Fback%2Fqq"
        val firstResponse = OkHttpClientUtils.get(url)
        firstResponse.close()
        val addCookie = OkHttpClientUtils.getCookie(firstResponse)
        val response = OkHttpClientUtils.get("https://ssl.ptlogin2.qq.com/pt_open_login?openlogin_data=which%3D%26refer_cgi%3Dauthorize%26response_type%3Dcode%26client_id%3D100495085%26state%3DbxlowqcqQF%26display%3D%26openapi%3D1010_1011%26switch%3D0%26src%3D1%26sdkv%3D%26sdkp%3Da%26tid%3D1594952757%26pf%3D%26need_pay%3D0%26browser%3D0%26browser_error%3D%26serial%3D%26token_key%3D%26redirect_uri%3Dhttps%253A%252F%252Fmusic.163.com%252Fback%252Fqq%26sign%3D%26time%3D%26status_version%3D%26status_os%3D%26status_machine%3D%26page_type%3D1%26has_auth%3D1%26update_auth%3D0%26auth_time%3D1594952758660&auth_token=${QQUtils.getToken2(qqEntity.superToken)}&pt_vcode_v1=0&pt_verifysession_v1=&verifycode=&u=${qqEntity.qq}&pt_randsalt=0&ptlang=2052&low_login_enable=0&u1=http%3A%2F%2Fconnect.qq.com&from_ui=1&fp=loginerroralert&device=2&aid=716027609&daid=383&pt_3rd_aid=100495085&ptredirect=1&h=1&g=1&pt_uistyle=35&regmaster=&", OkHttpClientUtils.addHeaders(
                "cookie", qqEntity.getCookieWithSuper() + addCookie,
                "user-agent", OkHttpClientUtils.PC_UA,
                "referer", url
        ))
        val str = OkHttpClientUtils.getStr(response)
        val commonResult = QQUtils.getResultUrl(str)
        return if (commonResult.code == 200){
            val resultResponse = OkHttpClientUtils.get(commonResult.t, OkHttpClientUtils.addReferer(url))
            resultResponse.close()
            val cookieMap = OkHttpClientUtils.getCookie(resultResponse, "MUSIC_U", "__csrf")
            if (cookieMap.size != 2) CommonResult(500, "没有取到cookie，登录失败，请稍后再试！！")
            else CommonResult(200, "", NeTeaseEntity(null, qqEntity.qq, "", "", cookieMap.getValue("MUSIC_U"), cookieMap.getValue("__csrf")))
        }else CommonResult(500, commonResult.msg)
    }

    override fun sign(neTeaseEntity: NeTeaseEntity): String {
        val response = OkHttpClientUtils.post("https://music.163.com/weapi/point/dailyTask", OkHttpClientUtils.addForms(
                *this.prepare(mapOf("type" to "0"))
        ), OkHttpClientUtils.addHeaders(
                "referer", referer,
                "cookie", neTeaseEntity.getCookie()
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return when (jsonObject.getInteger("code")){
            200 -> "签到成功！！"
            -1 -> "今日已签到"
            else -> jsonObject.getString("msg")
        }
    }

    private fun recommend(neTeaseEntity: NeTeaseEntity): CommonResult<List<String>>{
        val response = OkHttpClientUtils.post("https://music.163.com/weapi/v1/discovery/recommend/resource", OkHttpClientUtils.addForms(
                *this.prepare(mapOf("csrf_token" to neTeaseEntity.__csrf))
        ), OkHttpClientUtils.addHeaders(
                "cookie", neTeaseEntity.getCookie(),
                "referer", referer,
                "user-agent", OkHttpClientUtils.PC_UA
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return if (jsonObject.getInteger("code") == 200) {
            val jsonArray = jsonObject.getJSONArray("recommend")
            val list = mutableListOf<String>()
            for (i in jsonArray.indices) {
                val singleJsonObject = jsonArray.getJSONObject(i)
                list.add(singleJsonObject.getString("id"))
            }
            CommonResult(200, "", list)
        } else if (jsonObject.getInteger("code") == 301) CommonResult(500, "您的网易cookie已失效，请重新登录！！")
        else CommonResult(500, jsonObject.getString("msg"))
    }

    private fun getSongId(neTeaseEntity: NeTeaseEntity, playListId: String): JSONArray{
        val response = OkHttpClientUtils.post("https://music.163.com/weapi/v3/playlist/detail?csrf_token=", OkHttpClientUtils.addForms(
                *this.prepare(mapOf("id" to playListId, "n" to "1000", "csrf_token" to ""))
        ), OkHttpClientUtils.addHeaders(
                "cookie", neTeaseEntity.getCookie(),
                "referer", referer,
                "user-agent", OkHttpClientUtils.PC_UA
        ))
        val jsonObject = OkHttpClientUtils.getJson(response)
        return jsonObject.getJSONObject("playlist").getJSONArray("trackIds")
    }

    override fun listeningVolume(neTeaseEntity: NeTeaseEntity): String {
        val recommend = this.recommend(neTeaseEntity)
        return if (recommend.code == 200){
            val playList = recommend.t
            val ids = JSONArray()
            while (ids.size < 310){
                val songIds = this.getSongId(neTeaseEntity, playList[Random.nextInt(playList.size)])
                var k = 0
                while (ids.size < 310 && k < songIds.size){
                    val jsonObject = JSONObject()
                    jsonObject["download"] = 0
                    jsonObject["end"] = "playend"
                    jsonObject["id"] = songIds.getJSONObject(k).getInteger("id")
                    jsonObject["sourceId"] = ""
                    jsonObject["time"] = 240
                    jsonObject["type"] = "song"
                    jsonObject["wifi"] = 0
                    val totalJsonObject = JSONObject()
                    totalJsonObject["json"] = jsonObject
                    totalJsonObject["action"] = "play"
                    ids.add(totalJsonObject)
                    k++
                }
            }
            val response = OkHttpClientUtils.post("http://music.163.com/weapi/feedback/weblog", OkHttpClientUtils.addForms(
                    *this.prepare(mapOf("logs" to ids.toString()))
            ), OkHttpClientUtils.addHeaders(
                    "cookie", neTeaseEntity.getCookie(),
                    "referer", referer,
                    "user-agent", OkHttpClientUtils.PC_UA
            ))
            val jsonObject = OkHttpClientUtils.getJson(response)
            if (jsonObject.getInteger("code") == 200) "刷每日300听歌量成功！！"
            else jsonObject.getString("message")
        }else recommend.msg
    }
}