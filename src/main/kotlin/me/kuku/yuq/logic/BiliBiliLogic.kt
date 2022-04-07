package me.kuku.yuq.logic

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.pojo.Result
import me.kuku.pojo.ResultStatus
import me.kuku.pojo.UA
import me.kuku.utils.*
import okhttp3.MultipartBody
import okio.ByteString

object BiliBiliLogic {

    suspend fun getIdByName(username: String): Result<List<BiliBiliPojo>> {
        val enUsername = username.toUrlEncode()
        val jsonObject = OkHttpKtUtils.getJsonp("https://api.bilibili.com/x/web-interface/search/type?context=&search_type=bili_user&page=1&order=&keyword=$enUsername&category_id=&user_type=&order_sort=&changing=mid&__refresh__=true&_extra=&highlight=1&single_column=0&jsonp=jsonp&callback=__jp2",
            OkUtils.referer("https://search.bilibili.com/topic?keyword=$enUsername"))
        val dataJsonObject = jsonObject.getJSONObject("data")
        return if (dataJsonObject.getInteger("numResults") != 0) {
            val jsonArray = dataJsonObject.getJSONArray("result")
            val list = mutableListOf<BiliBiliPojo>()
            for (obj in jsonArray) {
                val singleJsonObject = obj as JSONObject
                list.add(
                    BiliBiliPojo(userId = singleJsonObject.getString("mid"),
                    name = singleJsonObject.getString("uname"))
                )
            }
            Result.success(list)
        } else Result.failure("not result")
    }

    private fun convert(jsonObject: JSONObject): BiliBiliPojo {
        val biliBiliPojo = BiliBiliPojo()
        val descJsonObject = jsonObject.getJSONObject("desc")
        val infoJsonObject = descJsonObject.getJSONObject("user_profile").getJSONObject("info")
        val forwardJsonObject = descJsonObject.getJSONObject("origin")
        biliBiliPojo.userId = infoJsonObject.getString("uid")
        biliBiliPojo.name = infoJsonObject.getString("uname")
        biliBiliPojo.id = descJsonObject.getString("dynamic_id")
        biliBiliPojo.rid = descJsonObject.getString("rid")
        biliBiliPojo.time = (descJsonObject.getString("timestamp") + "000").toLong()
        biliBiliPojo.bvId = descJsonObject.getString("bvid") ?: "没有发现bv号"
        biliBiliPojo.isForward = forwardJsonObject != null
        if (forwardJsonObject != null) {
            biliBiliPojo.forwardBvId = forwardJsonObject.getString("bvid") ?: "没有bvId"
            forwardJsonObject.getString("timestamp")?.let {
                biliBiliPojo.forwardTime = (it + "000").toLong()
            }
            biliBiliPojo.forwardId = forwardJsonObject.getString("dynamic_id")
        }
        val cardStr = jsonObject.getString("card")
        val cardJsonObject = JSON.parseObject(cardStr)
        var text: String? = null
        if (cardJsonObject != null) {
            val itemJsonObject = cardJsonObject.getJSONObject("item")
            text = cardJsonObject.getString("dynamic")
            val picList = biliBiliPojo.picList
            if (biliBiliPojo.bvId.isNotEmpty()) {
                cardJsonObject.getString("pic")?.let {
                    picList.add(it)
                }
            }
            if (itemJsonObject != null) {
                if (text == null) text = itemJsonObject.getString("description")
                if (text == null) text = itemJsonObject.getString("content")
                itemJsonObject.getJSONArray("pictures")?.forEach {
                    val picJsonObject = it as JSONObject
                    picList.add(picJsonObject.getString("img_src"))
                }
            }
            if (text == null) {
                cardJsonObject.getJSONObject("vest")?.let {
                    text = it.getString("content")
                }
            }
            if (text == null && cardJsonObject.containsKey("title")) {
                text = cardJsonObject.getString("title") + "------" + cardJsonObject.getString("summary")
            }
            val originStr = cardJsonObject.getString("origin")
            if (originStr != null) {
                val forwardPicList = biliBiliPojo.forwardPicList
                val forwardContentJsonObject = JSON.parseObject(originStr)
                if (biliBiliPojo.forwardBvId.isNotEmpty()) {
                    forwardContentJsonObject.getString("pic")?.let {
                        forwardPicList.add(it)
                    }
                }
                if (forwardContentJsonObject.containsKey("item")) {
                    val forwardItemJsonObject = forwardContentJsonObject.getJSONObject("item")
                    biliBiliPojo.forwardText = forwardItemJsonObject.getString("description")
                    if (biliBiliPojo.forwardText.isEmpty())
                        biliBiliPojo.forwardText = forwardItemJsonObject.getString("content")
                    val forwardPicJsonArray = forwardItemJsonObject.getJSONArray("pictures")
                    if (forwardPicJsonArray != null) {
                        for (obj in forwardPicJsonArray) {
                            val picJsonObject = obj as JSONObject
                            forwardPicList.add(picJsonObject.getString("img_src"))
                        }
                    }
                    val forwardUserJsonObject = forwardContentJsonObject.getJSONObject("user")
                    if (forwardUserJsonObject != null) {
                        biliBiliPojo.forwardUserId = forwardUserJsonObject.getString("uid")
                        biliBiliPojo.forwardName = forwardUserJsonObject.getString("name") ?: forwardUserJsonObject.getString("uname")
                    } else {
                        val forwardOwnerJsonObject = forwardContentJsonObject.getJSONObject("owner")
                        if (forwardOwnerJsonObject != null) {
                            biliBiliPojo.forwardUserId = forwardOwnerJsonObject.getString("mid")
                            biliBiliPojo.forwardName = forwardOwnerJsonObject.getString("name")

                        }
                    }
                } else {
                    biliBiliPojo.forwardText = forwardContentJsonObject.getString("dynamic") ?: "没有动态内容"
                    val forwardOwnerJsonObject = forwardContentJsonObject.getJSONObject("owner")
                    if (forwardOwnerJsonObject != null) {
                        biliBiliPojo.forwardUserId = forwardOwnerJsonObject.getString("mid")
                        biliBiliPojo.forwardName = forwardOwnerJsonObject.getString("name")
                    } else {
                        biliBiliPojo.forwardName = forwardContentJsonObject.getString("uname")
                        biliBiliPojo.forwardUserId = forwardContentJsonObject.getString("uid")
                        biliBiliPojo.forwardText = forwardContentJsonObject.getString("title")
                    }
                }
            }
        }
        biliBiliPojo.text = text ?: "无"
        val type = if (biliBiliPojo.bvId.isEmpty()) {
            if (biliBiliPojo.picList.isEmpty()) 17
            else 11
        }else 1
        biliBiliPojo.type = type
        return biliBiliPojo
    }

    fun convertStr(biliBiliPojo: BiliBiliPojo): String {
        val pattern = "yyyy-MM-dd HH:mm:ss"
        val bvId = biliBiliPojo.bvId
        val forwardBvId = biliBiliPojo.forwardBvId
        var ss = """
            ${biliBiliPojo.name}
            发布时间：${DateTimeFormatterUtils.format(biliBiliPojo.time, pattern)}
            内容：${biliBiliPojo.text}
            动态链接：https://t.bilibili.com/${biliBiliPojo.id}
            视频链接：${if (bvId.isEmpty()) "https://www.bilibili.com/video/$bvId" else "无"}
        """.trimIndent()
        if (biliBiliPojo.isForward) {
            ss += """
                转发自：${biliBiliPojo.forwardName}
                发布时间：${DateTimeFormatterUtils.format(biliBiliPojo.forwardTime, pattern)}
                内容：${biliBiliPojo.forwardText}
                动态链接：https://t.bilibili.com/${biliBiliPojo.forwardId}
                视频链接：${if (forwardBvId.isEmpty()) "https://www.bilibili.com/video/$forwardBvId" else "无"}
            """.trimIndent()
        }
        return ss
    }

    suspend fun getDynamicById(id: String, offsetId: String = "0"): Result<List<BiliBiliPojo>> {
        val jsonObject = OkHttpKtUtils.getJson("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=$id&offset_dynamic_id=$offsetId&need_top=1",
            OkUtils.referer("https://space.bilibili.com/$id/dynamic"))
        // next_offset  下一页开头
        val dataJsonObject = jsonObject.getJSONObject("data")
        val jsonArray = dataJsonObject.getJSONArray("cards") ?: return Result.failure(ResultStatus.DYNAMIC_NOT_FOUNT)
        val list = mutableListOf<BiliBiliPojo>()
        for (obj in jsonArray) {
            val singleJsonObject = obj as JSONObject
            val extraJsonObject = singleJsonObject.getJSONObject("extra")
            if (extraJsonObject != null && 1 == extraJsonObject.getInteger("is_space_top")) continue
            list.add(convert(singleJsonObject))
        }
        return Result.success(dataJsonObject.getString("next_offset"), list)
    }

    suspend fun loginByQr1(): String {
        val jsonObject = OkHttpKtUtils.getJson("https://passport.bilibili.com/qrcode/getLoginUrl")
        return jsonObject.getJSONObject("data").getString("url")
    }

    suspend fun loginByQr2(url: String): Result<BiliBiliEntity> {
        val oauthKey = MyUtils.regex("(?<=oauthKey\\=).*", url)
            ?: return Result.failure("链接格式不正确", null)
        val map = mutableMapOf("oauthKey" to oauthKey, "gourl" to "https://www.bilibili.com")
        val jsonObject = OkHttpKtUtils.postJson("https://passport.bilibili.com/qrcode/getLoginInfo", map)
        val status = jsonObject.getBoolean("status")
        return if (!status) {
            when (jsonObject.getInteger("data")) {
                -2 -> Result.failure("您的二维码已过期！！", null)
                -4 -> Result.failure(ResultStatus.QRCODE_NOT_SCANNED)
                -5 -> Result.failure(ResultStatus.QRCODE_IS_SCANNED)
                else -> Result.failure(jsonObject.getString("message"), null)
            }
        } else {
            val successUrl = jsonObject.getJSONObject("data").getString("url")
            val response = OkHttpKtUtils.get(successUrl, OkUtils.referer("https://passport.bilibili.com/login")).apply { close() }
            val cookie = OkUtils.cookie(response)
            val token = MyUtils.regex("bili_jct=", "; ", cookie)!!
            val locationUrl = response.header("location")!!
            val userid = MyUtils.regex("DedeUserID=", "&", locationUrl)!!
            val biliBiliEntity = BiliBiliEntity()
            biliBiliEntity.cookie = cookie
            biliBiliEntity.userid = userid
            biliBiliEntity.token = token
            Result.success(biliBiliEntity)
        }
    }

    suspend fun friendDynamic(biliBiliEntity: BiliBiliEntity): Result<List<BiliBiliPojo>> {
        val jsonObject = OkHttpKtUtils.getJson("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new?type_list=268435455",
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") != 0)  Result.failure(ResultStatus.COOKIE_EXPIRED)
        else {
            val list = mutableListOf<BiliBiliPojo>()
            jsonObject.getJSONObject("data").getJSONArray("cards").forEach{
                list.add(convert(it as JSONObject))
            }
            Result.success(list)
        }
    }

    suspend fun live(id: String): BiliBiliLive {
        val jsonObject = OkHttpKtUtils.getJsonp("https://api.bilibili.com/x/space/acc/info?mid=$id&jsonp=jsonp",
            OkUtils.referer("https://space.bilibili.com/$id/"))
        val dataJsonObject = jsonObject.getJSONObject("data")?.getJSONObject("live_room") ?: return BiliBiliLive(status = false)
        val status = dataJsonObject.getInteger("liveStatus")
        return BiliBiliLive(dataJsonObject.getString("title"), id, dataJsonObject.getString("url"), status == 1)
    }

    suspend fun liveSign(biliBiliEntity: BiliBiliEntity): Result<Void> {
        val jsonObject = OkHttpKtUtils.getJson("https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign",
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") == 0) Result.success()
        else Result.failure(jsonObject.getString("message"))
    }

    suspend fun like(biliBiliEntity: BiliBiliEntity, id: String, isLike: Boolean): Result<Void> {
        val map = mapOf("uid" to biliBiliEntity.userid, "dynamic_id" to id,
            "up" to if (isLike) "1" else "2", "csrf_token" to biliBiliEntity.token)
        val jsonObject = OkHttpKtUtils.postJson("https://api.vc.bilibili.com/dynamic_like/v1/dynamic_like/thumb", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") == 0) Result.success()
        else Result.failure("赞动态失败，${jsonObject.getString("message")}")
    }

    suspend fun comment(biliBiliEntity: BiliBiliEntity, rid: String, type: String, content: String): Result<Void> {
        val map = mapOf("oid" to rid, "type" to type, "message" to content, "plat" to "1",
            "jsoup" to "jsoup", "csrf_token" to biliBiliEntity.token)
        val jsonObject = OkHttpKtUtils.postJson("https://api.bilibili.com/x/v2/reply/add", map, OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") == 0) Result.success()
        else Result.failure("评论动态失败，${jsonObject.getString("message")}")
    }

    suspend fun forward(biliBiliEntity: BiliBiliEntity, id: String, content: String): Result<Void> {
        val map = mapOf("uid" to biliBiliEntity.userid, "dynamic_id" to id,
            "content" to content, "extension" to "{\"emoji_type\":1}", "at_uids" to "", "ctrl" to "[]",
            "csrf_token" to biliBiliEntity.token)
        val jsonObject = OkHttpKtUtils.postJson("https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/repost", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") == 0) Result.success()
        else Result.failure("转发动态失败，${jsonObject.getString("message")}")
    }

    suspend fun tossCoin(biliBiliEntity: BiliBiliEntity, rid: String, count: Int = 1): Result<Void> {
        val map = mapOf("aid" to rid, "multiply" to count.toString(), "select_like" to "1",
            "cross_domain" to "true", "csrf" to biliBiliEntity.token)
        val jsonObject = OkHttpKtUtils.postJson("https://api.bilibili.com/x/web-interface/coin/add", map,
            OkUtils.headers(biliBiliEntity.cookie, "https://www.bilibili.com/video/"))
        return if (jsonObject.getInteger("code") == 0) Result.success()
        else Result.failure("对该动态（视频）投硬币失败，${jsonObject.getString("message")}")
    }

    suspend fun favorites(biliBiliEntity: BiliBiliEntity, rid: String, name: String): Result<Void> {
        val userid = biliBiliEntity.userid
        val cookie = biliBiliEntity.cookie
        val token = biliBiliEntity.token
        val firstJsonObject = OkHttpKtUtils.getJson("https://api.bilibili.com/x/v3/fav/folder/created/list-all?type=2&rid=$rid&up_mid=$userid",
            OkUtils.cookie(cookie))
        if (firstJsonObject.getInteger("code") != 0) return Result.failure("收藏失败，请重新绑定哔哩哔哩")
        val jsonArray = firstJsonObject.getJSONObject("data").getJSONArray("list")
        var favoriteId: String? = null
        for (obj in jsonArray) {
            val jsonObject = obj as JSONObject
            if (jsonObject.getString("title") == name) {
                favoriteId = jsonObject.getString("id")
            }
        }
        if (favoriteId == null) {
            val map = mapOf("title" to name, "privacy" to "0", "jsonp" to "jsonp", "csrf" to token)
            val jsonObject = OkHttpKtUtils.postJson("https://api.bilibili.com/x/v3/fav/folder/add", map,
                OkUtils.cookie(cookie))
            if (jsonObject.getInteger("code") != 0) return Result.failure("您并没有该收藏夹，而且创建该收藏夹失败，请重试！！")
            favoriteId = jsonObject.getJSONObject("data").getString("id")
        }
        val map = mapOf("rid" to rid, "type" to "2", "add_media_ids" to favoriteId!!,
            "del_media_ids" to "", "jsonp" to "jsonp", "csrf" to token)
        val jsonObject = OkHttpKtUtils.postJson("https://api.bilibili.com/x/v3/fav/resource/deal", map,
            OkUtils.cookie(cookie))
        return if (jsonObject.getInteger("code").equals(0)) Result.success()
        else Result.failure("收藏视频失败，" + jsonObject.getString("message"))
    }

    private suspend fun uploadImage(biliBiliEntity: BiliBiliEntity, byteString: ByteString): Result<JSONObject> {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file_up", "123.jpg", OkUtils.stream(byteString))
            .addFormDataPart("biz", "draw")
            .addFormDataPart("category", "daily").build()
        val jsonObject = OkHttpKtUtils.postJson("https://api.vc.bilibili.com/api/v1/drawImage/upload", body,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") == 0) Result.success(jsonObject.getJSONObject("data"))
        else Result.failure("图片上传失败，" + jsonObject.getString("message"), null)
    }

    suspend fun publishDynamic(biliBiliEntity: BiliBiliEntity, content: String, images: List<String>): Result<Void> {
        val jsonArray = JSONArray()
        images.forEach{
            jsonArray.add(uploadImage(biliBiliEntity, OkHttpKtUtils.getByteString(it)))
        }
        val map = mapOf("biz" to "3", "category" to "3", "type" to "0", "pictures" to jsonArray.toString(),
            "title" to "", "tags" to "", "description" to content, "content" to content, "setting" to "{\"copy_forbidden\":0,\"cachedTime\":0}",
            "from" to "create.dynamic.web", "up_choose_comment" to "0", "extension" to "{\"emoji_type\":1,\"from\":{\"emoji_type\":1},\"flag_cfg\":{}}",
            "at_uids" to "", "at_control" to "", "csrf_token" to biliBiliEntity.token)
        val jsonObject = OkHttpKtUtils.postJson("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/create_draw", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") == 0) Result.success()
        else Result.failure("发布动态失败，" + jsonObject.getString("message"))
    }

    suspend fun ranking(): List<BiliBiliRanking> {
        val jsonObject = OkHttpKtUtils.getJson("https://api.bilibili.com/x/web-interface/ranking/v2?rid=0&type=all")
        val jsonArray = jsonObject.getJSONObject("data").getJSONArray("list")
        val list = mutableListOf<BiliBiliRanking>()
        for (obj in jsonArray) {
            val singleJsonObject = obj as JSONObject
            val biliBiliRanking = BiliBiliRanking()
            biliBiliRanking.aid = singleJsonObject.getString("aid")
            biliBiliRanking.cid = singleJsonObject.getString("cid")
            biliBiliRanking.title = singleJsonObject.getString("title")
            biliBiliRanking.desc = singleJsonObject.getString("desc")
            biliBiliRanking.username = singleJsonObject.getJSONObject("owner").getString("name")
            biliBiliRanking.dynamic = singleJsonObject.getString("dynamic")
            biliBiliRanking.bv = singleJsonObject.getString("bvid")
            list.add(biliBiliRanking)
        }
        return list
    }

    suspend fun report(biliBiliEntity: BiliBiliEntity, aid: String, cid: String, proGRes: Int): Result<Void> {
        val map = mapOf("aid" to aid, "cid" to cid, "progres" to proGRes.toString(),
            "csrf" to biliBiliEntity.token)
        val jsonObject = OkHttpKtUtils.postJson("http://api.bilibili.com/x/v2/history/report", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") == 0) Result.success()
        else Result.failure(jsonObject.getString("message"))
    }

    suspend fun share(biliBiliEntity: BiliBiliEntity, aid: String): Result<Void> {
        val map = mapOf("aid" to aid, "csrf" to biliBiliEntity.token)
        val jsonObject = OkHttpKtUtils.postJson("https://api.bilibili.com/x/web-interface/share/add", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonObject.getInteger("code") == 0) Result.success()
        else Result.failure(jsonObject.getString("message"))
    }

    suspend fun getReplay(biliBiliEntity: BiliBiliEntity, oid: String, page: Int): List<BiliBiliReplay> {
        val jsonObject = OkHttpKtUtils.getJsonp(
            "https://api.bilibili.com/x/v2/reply?callback=jQuery17207366906764958399_${System.currentTimeMillis()}&jsonp=jsonp&pn=$page&type=1&oid=$oid&sort=2&_=${System.currentTimeMillis()}",
            OkUtils.headers(biliBiliEntity.cookie, "https://www.bilibili.com/"))
        return if (jsonObject.getInteger("code") == 0) {
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("replies")
            val list = mutableListOf<BiliBiliReplay>()
            for (obj in jsonArray) {
                val singleJsonObject = obj as JSONObject
                val biliReplay = BiliBiliReplay(singleJsonObject.getString("rpid"), singleJsonObject.getJSONObject("content").getString("message"))
                list.add(biliReplay)
            }
            list
        }else listOf()
    }

    suspend fun reportComment(biliBiliEntity: BiliBiliEntity, oid: String, rpId: String, reason: Int): Result<Void> {
        // 违法违规 9   色情  2    低俗 10    赌博诈骗  12
        // 人身攻击  7   侵犯隐私 15
        // 垃圾广告 1   引战 4    剧透   5    刷屏   3      抢楼 16    内容不相关   8     青少年不良信息  17
        //  其他 0
        val map = mapOf("oid" to oid, "type" to "1", "rpid" to rpId, "reason" to reason.toString(),
            "content" to "", "ordering" to "heat", "jsonp" to "jsonp", "csrf" to biliBiliEntity.token)
        val jsonObject = OkHttpKtUtils.postJson("https://api.bilibili.com/x/v2/reply/report", map,
            OkUtils.headers(biliBiliEntity.cookie, "https://www.bilibili.com/"))
        return if (jsonObject.getInteger("code") == 0) Result.success("举报评论成功！！", null)
        else Result.failure("举报评论失败！！")
    }

    suspend fun getOidByBvId(bvId: String): String {
        val html = OkHttpKtUtils.getStr("https://www.bilibili.com/video/$bvId",
            OkUtils.ua(UA.PC))
        val jsonStr = MyUtils.regex("INITIAL_STATE__=", ";\\(function\\(\\)", html)
        val jsonObject = JSON.parseObject(jsonStr)
        return jsonObject.getString("aid")
    }

    suspend fun followed(biliBiliEntity: BiliBiliEntity): Result<List<BiliBiliFollowed>> {
        val list = mutableListOf<BiliBiliFollowed>()
        var i = 1
        while (true) {
            val jsonObject = onceFollowed(biliBiliEntity, i++)
            if (jsonObject.getInteger("code") == 0) {
                val jsonArray = jsonObject.getJSONObject("data").getJSONArray("list")
                if (jsonArray.size == 0) break
                for (any in jsonArray) {
                    val it = any as JSONObject
                    list.add(BiliBiliFollowed(it.getString("mid"), it.getString("uname")))
                }
            } else return Result.failure(jsonObject.getString("message"))
        }
        return Result.success(list)
    }

    private suspend fun onceFollowed(biliBiliEntity: BiliBiliEntity, i: Int): JSONObject {
        val headers = mapOf("referer" to "https://space.bilibili.com/${biliBiliEntity.userid}/fans/follow",
            "user-agent" to UA.PC.value, "cookie" to biliBiliEntity.cookie)
        return OkHttpKtUtils.getJsonp("https://api.bilibili.com/x/relation/followings?vmid=${biliBiliEntity.userid}&pn=$i&ps=100&order=desc&order_type=attention&jsonp=jsonp&callback=__jp5",
            headers)
    }


}

data class BiliBiliPojo(
    var userId: String = "",
    var name: String = "",
    var id: String = "",
    var rid: String = "",
    var type: Int = -1,
    var time: Long = 0,
    var text: String = "",
    var bvId: String = "",
    var picList: MutableList<String> = mutableListOf(),
    var isForward: Boolean = false,
    var forwardUserId: String = "",
    var forwardName: String = "",
    var forwardId: String = "",
    var forwardTime: Long = 0,
    var forwardText: String = "",
    var forwardBvId: String = "",
    var forwardPicList: MutableList<String> = mutableListOf()
)

data class BiliBiliLive(
    var title: String = "",
    var id: String = "",
    var url: String = "",
    var status: Boolean = false
)

data class BiliBiliRanking(
    var aid: String = "",
    var cid: String = "",
    var title: String = "",
    var desc: String = "",
    var username: String = "",
    var dynamic: String = "",
    var bv: String = ""
)

data class BiliBiliReplay(
    var id: String = "",
    var content: String = ""
)

data class BiliBiliFollowed(
    var id: String = "",
    var name: String = ""
)