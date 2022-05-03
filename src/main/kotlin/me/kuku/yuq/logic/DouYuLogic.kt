package me.kuku.yuq.logic

import com.alibaba.fastjson.JSONObject
import me.kuku.pojo.BaseResult
import me.kuku.pojo.UA
import me.kuku.utils.*
import me.kuku.yuq.entity.DouYuEntity
import org.springframework.stereotype.Service

@Service
class DouYuLogic {

    private val qqApp = QqApp(716027609, 383, 101047385)


    suspend fun getQrcode(): DouYuQqQrcode {
        val response = OkHttpKtUtils.get("https://www.douyu.com/member/oauth/signin/qq?biz_type=1&ref_url=https%3A%2F%2Fwww.douyu.com%2F&room_id=0&cate_id=0&tag_id=0&child_id=0&vid=0&fac=&type=login&isMultiAccount=0").also { it.close() }
        val location = response.header("location")!!
        val cookie = OkUtils.cookie(response)
        val state = MyUtils.regex("state=", "&", location)
        val qrcode = QqQrCodeLoginUtils.getQrcode(qqApp)
        return DouYuQqQrcode(qrcode, state!!, cookie)
    }


    suspend fun checkQrcode(douYuQqQrcode: DouYuQqQrcode): BaseResult<DouYuEntity> {
        val ss = QqQrCodeLoginUtils.authorize(qqApp, douYuQqQrcode.qqLoginQrcode.sig, douYuQqQrcode.state, "https://www.douyu.com/member/oauth/signin/qq")
        return if (ss.isFailure) BaseResult.failure(code = ss.code,  message = ss.message)
        else {
            val url = ss.data
            val headers = OkUtils.headers(douYuQqQrcode.cookie, "https://graph.qq.com/", UA.PC)
            val firstResponse = OkHttpKtUtils.get(url, headers).apply { close() }
            val firstUrl = firstResponse.header("location")!!
            val secondResponse = OkHttpKtUtils.get(firstUrl, headers).apply { close() }
            val secondUrl = secondResponse.header("location")!!
            val thirdResponse = OkHttpKtUtils.get("https:$secondUrl", headers).apply { close() }
            val cookie = OkUtils.cookie(thirdResponse)
            BaseResult.success(DouYuEntity().also {
                it.cookie = cookie
            })
        }
    }

    suspend fun room(douYuEntity: DouYuEntity): BaseResult<List<DouYuRoom>> {
        var i = 1
        val resultList = mutableListOf<DouYuRoom>()
        while (true) {
            val jsonObject = OkHttpKtUtils.getJson("https://www.douyu.com/wgapi/livenc/liveweb/follow/list?sort=0&cid1=0&page=${i++}",
                OkUtils.headers(douYuEntity.cookie, "", UA.PC))
            if (jsonObject.getInteger("error") == 0) {
                val list = jsonObject.getJSONObject("data").getJSONArray("list")?.map { it as JSONObject } ?: break
                for (singleJsonObject in list) {
                    val douYuRoom = DouYuRoom(singleJsonObject.getString("room_name"), singleJsonObject.getString("nickname"),
                        "https://www.douyu.com${singleJsonObject.getString("url")}", singleJsonObject.getString("game_name"), singleJsonObject.getInteger("show_status") == 2,
                        singleJsonObject.getString("online"), singleJsonObject.getLong("room_id"))
                    resultList.add(douYuRoom)
                }
            } else return BaseResult.failure(jsonObject.getString("msg"))
        }
        return BaseResult.success(resultList)
    }

}

data class DouYuQqQrcode(val qqLoginQrcode: QqLoginQrcode, val state: String, val cookie: String)

data class DouYuRoom(val name: String, val nickName: String, val url: String, val gameName: String, val showStatus: Boolean, val online: String, val roomId: Long)