package me.kuku.yuq.logic

import com.alibaba.fastjson.JSONObject
import me.kuku.pojo.BaseResult
import me.kuku.pojo.UA
import me.kuku.utils.*
import me.kuku.yuq.entity.HuYaEntity
import org.springframework.stereotype.Service

@Service
class HuYaLogic {

    private val qqApp = QqApp(716027609, 383, 101406359)

    suspend fun getQrcode(): HuYaQrcode {
        val response = OkHttpKtUtils.get("https://udb3lgn.huya.com/web/v2/signin?uri=30003&requestId=75854675&page=https%253A%252F%252Fwww.huya.com%252F&appid=5002&pageUrl=https%3A%2F%2Fwww.huya.com%2F&byPass=3&logLock=&logTest=&domain=huya.com&domainList=&exchange=${MyUtils.randomNum(8)}&guid=${MyUtils.randomLetterLowerNum(6).md5()}&action=1&style=1&busiurl=https%3A%2F%2Fwww.huya.com&type=qq&terminal=web&win=1&notOpen=").apply { close() }
        val url = response.header("location")!!
        val state = MyUtils.regex("state=", "&", url)!!.toUrlDecode()
        val qqLoginQrcode = QqQrCodeLoginUtils.getQrcode(qqApp)
        return HuYaQrcode(qqLoginQrcode, state)
    }

    suspend fun checkQrcode(huYaQrcode: HuYaQrcode): BaseResult<HuYaEntity> {
        val result = QqQrCodeLoginUtils.authorize(qqApp, huYaQrcode.qqLoginQrcode.sig, huYaQrcode.state, "https://udb3lgn.huya.com/web/v2/callback")
        return if (result.isSuccess) {
            val url = result.data
            val response = OkHttpKtUtils.get(url, OkUtils.referer("https://graph.qq.com")).apply { close() }
            val ss = OkUtils.cookie(response)
            BaseResult.success(HuYaEntity().also {
                it.cookie = ss
            })
        } else BaseResult.failure(result.message, null, result.code)
    }

    fun live(huYaEntity: HuYaEntity): BaseResult<List<HuYaLive>> {
        var i = 0
        val resultList = mutableListOf<HuYaLive>()
        while (true) {
            val response = OkHttpUtils.get("https://live.huya.com/liveHttpUI/getUserSubscribeToInfoList?iPageIndex=${i++}&_=${System.currentTimeMillis()}",
                OkUtils.headers(huYaEntity.cookie, "", UA.PC))
            if (response.code == 200) {
                val jsonObject = OkUtils.json(response)
                val list = jsonObject.getJSONArray("vItems").map { it as JSONObject }
                if (list.isEmpty()) break
                for (ss in list) {
                    val huYaLive = HuYaLive(ss.getLong("iRoomId"), ss.getString("sLiveDesc"), ss.getString("sGameName"),
                        ss.getInteger("iIsLive") == 1, ss.getString("sNick"), ss.getString("sVideoCaptureUrl"), "https://www.huya.com/${ss.getLong("iRoomId")}")
                    resultList.add(huYaLive)
                }
            } else return BaseResult.failure<List<HuYaLive>>("查询失败，可能cookie已失效").also { response.close() }
        }
        return BaseResult.success(resultList)
    }

}

data class HuYaQrcode(val qqLoginQrcode: QqLoginQrcode, val state: String)

data class HuYaLive(val roomId: Long, val liveDesc: String, val gameName: String, val isLive: Boolean, val nick: String, val videoCaptureUrl: String, val url: String)