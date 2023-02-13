package me.kuku.mirai.logic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.contains
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import me.kuku.pojo.CommonResult
import me.kuku.pojo.ResultStatus
import me.kuku.pojo.UA
import me.kuku.mirai.entity.BiliBiliEntity
import me.kuku.mirai.utils.ffmpeg
import me.kuku.utils.*
import okhttp3.MultipartBody
import okio.ByteString
import java.io.File

object BiliBiliLogic {

    suspend fun getIdByName(username: String): CommonResult<List<BiliBiliPojo>> {
        val enUsername = username.toUrlEncode()
        val jsonNode = OkHttpKtUtils.getJsonp("https://api.bilibili.com/x/web-interface/search/type?context=&search_type=bili_user&page=1&order=&keyword=$enUsername&category_id=&user_type=&order_sort=&changing=mid&__refresh__=true&_extra=&highlight=1&single_column=0&jsonp=jsonp&callback=__jp2",
            OkUtils.referer("https://search.bilibili.com/topic?keyword=$enUsername"))
        val dataJsonNode = jsonNode["data"]
        return if (dataJsonNode.getInteger("numCommonResults") != 0) {
            val jsonArray = dataJsonNode["result"]
            val list = mutableListOf<BiliBiliPojo>()
            for (obj in jsonArray) {
                list.add(
                    BiliBiliPojo(userId = obj.getString("mid"),
                        name = obj.getString("uname"))
                )
            }
            CommonResult.success(list)
        } else CommonResult.failure("not result")
    }

    private fun convert(jsonNode: JsonNode): BiliBiliPojo {
        val biliBiliPojo = BiliBiliPojo()
        val descJsonNode = jsonNode["desc"]
        val infoJsonNode = descJsonNode["user_profile"]?.get("info")
        val forwardJsonNode = descJsonNode["origin"]
        biliBiliPojo.userId = infoJsonNode?.get("uid")?.asText() ?: ""
        biliBiliPojo.name = infoJsonNode?.get("uname")?.asText() ?: ""
        biliBiliPojo.id = descJsonNode.getString("dynamic_id")
        biliBiliPojo.rid = descJsonNode.getString("rid")
        biliBiliPojo.time = (descJsonNode.getString("timestamp") + "000").toLong()
        biliBiliPojo.bvId = descJsonNode.get("bvid")?.asText() ?: ""
        biliBiliPojo.isForward = forwardJsonNode != null
        if (forwardJsonNode != null) {
            biliBiliPojo.forwardBvId = forwardJsonNode["bvid"]?.asText() ?: ""
            forwardJsonNode.get("timestamp")?.asText()?.let {
                biliBiliPojo.forwardTime = (it + "000").toLong()
            }
            biliBiliPojo.forwardId = forwardJsonNode.getString("dynamic_id")
        }
        var text: String? = null
        jsonNode["card"]?.asText()?.let { Jackson.parse(it) }?.let { cardJsonNode ->
            if (biliBiliPojo.userId.isEmpty()) {
                val collectionJsonNode = cardJsonNode["collection"]
                biliBiliPojo.userId = collectionJsonNode.getString("id")
                biliBiliPojo.name = collectionJsonNode.getString("name")
            }
            val itemJsonNode = cardJsonNode["item"]
            text = cardJsonNode["dynamic"]?.asText()
            val picList = biliBiliPojo.picList
            if (biliBiliPojo.bvId.isNotEmpty()) {
                cardJsonNode["pic"]?.asText()?.let {
                    picList.add(it)
                }
            }
            if (itemJsonNode != null) {
                if (text == null) text = itemJsonNode["description"]?.asText()
                if (text == null) text = itemJsonNode["content"]?.asText()
                itemJsonNode["pictures"]?.forEach {
                    picList.add(it.getString("img_src"))
                }
            }
            if (text == null) {
                cardJsonNode["vest"]?.let {
                    text = it.getString("content")
                }
            }
            if (text == null && cardJsonNode.contains("title")) {
                text = cardJsonNode.getString("title") + "------" + cardJsonNode.getString("summary")
            }
            cardJsonNode["pub_location"]?.asText()?.let { location ->
                biliBiliPojo.ipFrom = location
            }
            val originStr = cardJsonNode["origin"]?.asText()
            if (originStr != null) {
                val forwardPicList = biliBiliPojo.forwardPicList
                val forwardContentJsonNode = originStr.toJsonNode()
                if (biliBiliPojo.forwardBvId.isNotEmpty()) {
                    forwardContentJsonNode["pic"]?.let {
                        forwardPicList.add(it.asText())
                    }
                }
                if (forwardContentJsonNode.contains("item")) {
                    val forwardItemJsonNode = forwardContentJsonNode["item"]
                    biliBiliPojo.forwardText = forwardItemJsonNode["description"]?.asText() ?: ""
                    if (biliBiliPojo.forwardText.isEmpty())
                        biliBiliPojo.forwardText = forwardItemJsonNode.getString("content")
                    val forwardPicJsonArray = forwardItemJsonNode["pictures"]
                    if (forwardPicJsonArray != null) {
                        for (obj in forwardPicJsonArray) {
                            forwardPicList.add(obj.getString("img_src"))
                        }
                    }
                    val forwardUserJsonNode = forwardContentJsonNode["user"]
                    if (forwardUserJsonNode != null) {
                        biliBiliPojo.forwardUserId = forwardUserJsonNode.getString("uid")
                        biliBiliPojo.forwardName = forwardUserJsonNode["name"]?.asText() ?: forwardUserJsonNode.getString("uname")
                    } else {
                        val forwardOwnerJsonNode = forwardContentJsonNode["owner"]
                        if (forwardOwnerJsonNode != null) {
                            biliBiliPojo.forwardUserId = forwardOwnerJsonNode.getString("mid")
                            biliBiliPojo.forwardName = forwardOwnerJsonNode.getString("name")

                        }
                    }
                } else {
                    biliBiliPojo.forwardText = forwardContentJsonNode["dynamic"]?.asText() ?: "没有动态内容"
                    val forwardOwnerJsonNode = forwardContentJsonNode["owner"]
                    if (forwardOwnerJsonNode != null) {
                        biliBiliPojo.forwardUserId = forwardOwnerJsonNode["mid"]?.asText() ?: ""
                        biliBiliPojo.forwardName = forwardOwnerJsonNode["name"]?.asText() ?: ""
                    } else {
                        biliBiliPojo.forwardName = forwardContentJsonNode["uname"]?.asText() ?: ""
                        biliBiliPojo.forwardUserId = forwardContentJsonNode["uid"]?.asText() ?: ""
                        biliBiliPojo.forwardText = forwardContentJsonNode["title"]?.asText() ?: ""
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
        val ipFrom = biliBiliPojo.ipFrom
        val forwardBvId = biliBiliPojo.forwardBvId
        var ss = "#${biliBiliPojo.name}\n来自：${ipFrom.ifEmpty { "无" }}\n发布时间：${DateTimeFormatterUtils.format(biliBiliPojo.time, pattern)}" +
                "\n内容：${biliBiliPojo.text}\n动态链接：https://t.bilibili.com/${biliBiliPojo.id}\n视频链接：${if (bvId.isNotEmpty()) "https://www.bilibili.com/video/$bvId" else "无"}"
        if (biliBiliPojo.isForward) {
            ss += "\n转发自：#${biliBiliPojo.forwardName}\n发布时间：${DateTimeFormatterUtils.format(biliBiliPojo.forwardTime, pattern)}\n" +
                    "内容：${biliBiliPojo.forwardText}\n动态链接：https://t.bilibili.com/${biliBiliPojo.forwardId}\n视频链接：${if (forwardBvId.isNotEmpty()) "https://www.bilibili.com/video/$forwardBvId" else "无"}"
        }
        return ss
    }

    suspend fun videoByBvId(biliBiliEntity: BiliBiliEntity, bvId: String): File {
        val htmlUrl = "https://www.bilibili.com/video/$bvId/"
        val response = OkHttpKtUtils.get(htmlUrl, OkUtils.referer(biliBiliEntity.cookie))
        return if (response.code != 200) {
            response.close()
            error("错误：${response.code}")
        } else {
            val html = OkUtils.str(response)
            val jsonNode = MyUtils.regex("window.__playinfo__=", "</sc", html)?.toJsonNode() ?: error("未获取到内容")
            val videoUrl = jsonNode["data"]["dash"]["video"][0]["baseUrl"].asText()
            val audioUrl = jsonNode["data"]["dash"]["audio"][0]["baseUrl"].asText()
            val videoFile: File
            val audioFile: File
            OkHttpKtUtils.getByteStream(videoUrl, OkUtils.referer(htmlUrl)).use {
                videoFile = IOUtils.writeTmpFile("${bvId}.mp4", it, false)
            }
            OkHttpKtUtils.getByteStream(audioUrl, OkUtils.referer(htmlUrl)).use {
                audioFile = IOUtils.writeTmpFile("${bvId}.mp3", it, false)
            }
            val videoPath = videoFile.absolutePath
            val audioPath = audioFile.absolutePath
            val outputPath = videoPath.replace(bvId, "${bvId}output")
            ffmpeg("ffmpeg -i $videoPath -i $audioPath -c:v copy -c:a aac -strict experimental $outputPath")
            videoFile.delete()
            audioFile.delete()
            File(outputPath)
        }
    }

    suspend fun getDynamicById(id: String, offsetId: String = "0"): CommonResult<List<BiliBiliPojo>> {
        val jsonNode = OkHttpKtUtils.getJson("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=0&host_uid=$id&offset_dynamic_id=$offsetId&need_top=1",
            OkUtils.referer("https://space.bilibili.com/$id/dynamic"))
        // next_offset  下一页开头
        val dataJsonNode = jsonNode["data"]
        val jsonArray = dataJsonNode["cards"] ?: return CommonResult.failure(ResultStatus.DYNAMIC_NOT_FOUNT)
        val list = mutableListOf<BiliBiliPojo>()
        for (obj in jsonArray) {
            val extraJsonNode = obj["extra"]
            if (extraJsonNode != null && 1 == extraJsonNode.getInteger("is_space_top")) continue
            list.add(convert(obj))
        }
        return CommonResult.success(message = dataJsonNode.getString("next_offset"), data = list)
    }

    suspend fun loginByQr1(): String {
        val jsonNode = OkHttpKtUtils.getJson("https://passport.bilibili.com/qrcode/getLoginUrl")
        return jsonNode["data"]["url"].asText()
    }

    suspend fun loginByQr2(url: String): CommonResult<BiliBiliEntity> {
        val oauthKey = MyUtils.regex("(?<=oauthKey\\=).*", url)
            ?: return CommonResult.failure("链接格式不正确", null)
        val map = mutableMapOf("oauthKey" to oauthKey, "gourl" to "https://www.bilibili.com")
        val jsonNode = OkHttpKtUtils.postJson("https://passport.bilibili.com/qrcode/getLoginInfo", map)
        val status = jsonNode.getBoolean("status")
        return if (!status) {
            when (jsonNode.getInteger("data")) {
                -2 -> CommonResult.failure("您的二维码已过期！！", null)
                -4 -> CommonResult.failure(ResultStatus.QRCODE_NOT_SCANNED)
                -5 -> CommonResult.failure(ResultStatus.QRCODE_IS_SCANNED)
                else -> CommonResult.failure(jsonNode.getString("message"), null)
            }
        } else {
            val successUrl = jsonNode["data"]["url"].asText()
            val response = OkHttpKtUtils.get(successUrl, OkUtils.referer("https://passport.bilibili.com/login")).apply { close() }
            val cookie = OkUtils.cookie(response)
            val token = MyUtils.regex("bili_jct=", "; ", cookie)!!
            val locationUrl = response.header("location")!!
            val userid = MyUtils.regex("DedeUserID=", "&", locationUrl)!!
            val biliBiliEntity = BiliBiliEntity()
            biliBiliEntity.cookie = cookie
            biliBiliEntity.userid = userid
            biliBiliEntity.token = token
            CommonResult.success(biliBiliEntity)
        }
    }

    suspend fun friendDynamic(biliBiliEntity: BiliBiliEntity): CommonResult<List<BiliBiliPojo>> {
        val jsonNode = OkHttpKtUtils.getJson("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/dynamic_new?type_list=268435455",
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") != 0)  CommonResult.failure(ResultStatus.COOKIE_EXPIRED)
        else {
            val list = mutableListOf<BiliBiliPojo>()
            jsonNode["data"]["cards"].forEach{
                list.add(convert(it))
            }
            CommonResult.success(list)
        }
    }

    suspend fun live(id: String): BiliBiliLive {
        val jsonNode = client.get("https://api.bilibili.com/x/space/acc/info?mid=$id&jsonp=jsonp") {
            headers {
                referer("https://space.bilibili.com/$id/")
                userAgent(UA.PC.value)
            }
        }.body<JsonNode>()
        val dataJsonNode = jsonNode["data"]?.get("live_room") ?: return BiliBiliLive(status = false)
        val status = dataJsonNode.get("liveStatus")?.asInt()
        val title = dataJsonNode.get("title")?.asText() ?: ""
        val url = dataJsonNode.get("url")?.asText() ?: ""
        val imageUrl = dataJsonNode.get("cover")?.asText() ?: ""
        return BiliBiliLive(title, id, url, imageUrl, status == 1)
    }

    suspend fun liveSign(biliBiliEntity: BiliBiliEntity): String {
        val jsonNode = OkHttpKtUtils.getJson("https://api.live.bilibili.com/xlive/web-ucenter/v1/sign/DoSign",
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") == 0) "成功"
        else error(jsonNode.getString("message"))
    }

    suspend fun like(biliBiliEntity: BiliBiliEntity, id: String, isLike: Boolean): CommonResult<Void> {
        val map = mapOf("uid" to biliBiliEntity.userid, "dynamic_id" to id,
            "up" to if (isLike) "1" else "2", "csrf_token" to biliBiliEntity.token)
        val jsonNode = OkHttpKtUtils.postJson("https://api.vc.bilibili.com/dynamic_like/v1/dynamic_like/thumb", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") == 0) CommonResult.success()
        else CommonResult.failure("赞动态失败，${jsonNode.getString("message")}")
    }

    suspend fun comment(biliBiliEntity: BiliBiliEntity, rid: String, type: String, content: String): CommonResult<Void> {
        val map = mapOf("oid" to rid, "type" to type, "message" to content, "plat" to "1",
            "jsoup" to "jsoup", "csrf_token" to biliBiliEntity.token)
        val jsonNode = OkHttpKtUtils.postJson("https://api.bilibili.com/x/v2/reply/add", map, OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") == 0) CommonResult.success()
        else CommonResult.failure("评论动态失败，${jsonNode.getString("message")}")
    }

    suspend fun forward(biliBiliEntity: BiliBiliEntity, id: String, content: String): CommonResult<Void> {
        val map = mapOf("uid" to biliBiliEntity.userid, "dynamic_id" to id,
            "content" to content, "extension" to "{\"emoji_type\":1}", "at_uids" to "", "ctrl" to "[]",
            "csrf_token" to biliBiliEntity.token)
        val jsonNode = OkHttpKtUtils.postJson("https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/repost", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") == 0) CommonResult.success()
        else CommonResult.failure("转发动态失败，${jsonNode.getString("message")}")
    }

    suspend fun tossCoin(biliBiliEntity: BiliBiliEntity, rid: String, count: Int = 1): CommonResult<Void> {
        val map = mapOf("aid" to rid, "multiply" to count.toString(), "select_like" to "1",
            "cross_domain" to "true", "csrf" to biliBiliEntity.token)
        val jsonNode = OkHttpKtUtils.postJson("https://api.bilibili.com/x/web-interface/coin/add", map,
            OkUtils.headers(biliBiliEntity.cookie, "https://www.bilibili.com/video/"))
        return if (jsonNode.getInteger("code") == 0) CommonResult.success()
        else CommonResult.failure("对该动态（视频）投硬币失败，${jsonNode.getString("message")}")
    }

    suspend fun favorites(biliBiliEntity: BiliBiliEntity, rid: String, name: String): CommonResult<Void> {
        val userid = biliBiliEntity.userid
        val cookie = biliBiliEntity.cookie
        val token = biliBiliEntity.token
        val firstJsonNode = OkHttpKtUtils.getJson("https://api.bilibili.com/x/v3/fav/folder/created/list-all?type=2&rid=$rid&up_mid=$userid",
            OkUtils.cookie(cookie))
        if (firstJsonNode.getInteger("code") != 0) return CommonResult.failure("收藏失败，请重新绑定哔哩哔哩")
        val jsonArray = firstJsonNode["data"]["list"]
        var favoriteId: String? = null
        for (obj in jsonArray) {
            if (obj.getString("title") == name) {
                favoriteId = obj.getString("id")
            }
        }
        if (favoriteId == null) {
            val map = mapOf("title" to name, "privacy" to "0", "jsonp" to "jsonp", "csrf" to token)
            val jsonNode = OkHttpKtUtils.postJson("https://api.bilibili.com/x/v3/fav/folder/add", map,
                OkUtils.cookie(cookie))
            if (jsonNode.getInteger("code") != 0) return CommonResult.failure("您并没有该收藏夹，而且创建该收藏夹失败，请重试！！")
            favoriteId = jsonNode["data"]["id"].asText()
        }
        val map = mapOf("rid" to rid, "type" to "2", "add_media_ids" to favoriteId!!,
            "del_media_ids" to "", "jsonp" to "jsonp", "csrf" to token)
        val jsonNode = OkHttpKtUtils.postJson("https://api.bilibili.com/x/v3/fav/resource/deal", map,
            OkUtils.cookie(cookie))
        return if (jsonNode.getInteger("code") == 0) CommonResult.success()
        else CommonResult.failure("收藏视频失败，" + jsonNode.getString("message"))
    }

    private suspend fun uploadImage(biliBiliEntity: BiliBiliEntity, byteString: ByteString): CommonResult<JsonNode> {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file_up", "123.jpg", OkUtils.stream(byteString))
            .addFormDataPart("biz", "draw")
            .addFormDataPart("category", "daily").build()
        val jsonNode = OkHttpKtUtils.postJson("https://api.vc.bilibili.com/api/v1/drawImage/upload", body,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") == 0) CommonResult.success(jsonNode["data"])
        else CommonResult.failure("图片上传失败，" + jsonNode.getString("message"), null)
    }

    suspend fun publishDynamic(biliBiliEntity: BiliBiliEntity, content: String, images: List<String>): CommonResult<Void> {
        val jsonArray = Jackson.createArrayNode()
        images.forEach{
            jsonArray.addPOJO(uploadImage(biliBiliEntity, OkHttpKtUtils.getByteString(it)))
        }
        val map = mapOf("biz" to "3", "category" to "3", "type" to "0", "pictures" to jsonArray.toString(),
            "title" to "", "tags" to "", "description" to content, "content" to content, "setting" to "{\"copy_forbidden\":0,\"cachedTime\":0}",
            "from" to "create.dynamic.web", "up_choose_comment" to "0", "extension" to "{\"emoji_type\":1,\"from\":{\"emoji_type\":1},\"flag_cfg\":{}}",
            "at_uids" to "", "at_control" to "", "csrf_token" to biliBiliEntity.token)
        val jsonNode = OkHttpKtUtils.postJson("https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/create_draw", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") == 0) CommonResult.success()
        else CommonResult.failure("发布动态失败，" + jsonNode.getString("message"))
    }

    suspend fun ranking(): List<BiliBiliRanking> {
        val jsonNode = OkHttpKtUtils.getJson("https://api.bilibili.com/x/web-interface/ranking/v2?rid=0&type=all")
        val jsonArray = jsonNode["data"]["list"]
        val list = mutableListOf<BiliBiliRanking>()
        for (singleJsonNode in jsonArray) {
            val biliBiliRanking = BiliBiliRanking()
            biliBiliRanking.aid = singleJsonNode.getString("aid")
            biliBiliRanking.cid = singleJsonNode.getString("cid")
            biliBiliRanking.title = singleJsonNode.getString("title")
            biliBiliRanking.desc = singleJsonNode.getString("desc")
            biliBiliRanking.username = singleJsonNode["owner"]["name"].asText()
            biliBiliRanking.dynamic = singleJsonNode.getString("dynamic")
            biliBiliRanking.bv = singleJsonNode.getString("bvid")
            list.add(biliBiliRanking)
        }
        return list
    }

    suspend fun report(biliBiliEntity: BiliBiliEntity, aid: String, cid: String, proGRes: Int): String {
        val map = mapOf("aid" to aid, "cid" to cid, "progres" to proGRes.toString(),
            "csrf" to biliBiliEntity.token)
        val jsonNode = OkHttpKtUtils.postJson("http://api.bilibili.com/x/v2/history/report", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") == 0) "成功"
        else error(jsonNode.getString("message"))
    }

    suspend fun share(biliBiliEntity: BiliBiliEntity, aid: String): String {
        val map = mapOf("aid" to aid, "csrf" to biliBiliEntity.token)
        val jsonNode = OkHttpKtUtils.postJson("https://api.bilibili.com/x/web-interface/share/add", map,
            OkUtils.cookie(biliBiliEntity.cookie))
        return if (jsonNode.getInteger("code") == 0) "成功"
        else error(jsonNode.getString("message"))
    }

    suspend fun getReplay(biliBiliEntity: BiliBiliEntity, oid: String, page: Int): List<BiliBiliReplay> {
        val jsonNode = OkHttpKtUtils.getJsonp(
            "https://api.bilibili.com/x/v2/reply?callback=jQuery17207366906764958399_${System.currentTimeMillis()}&jsonp=jsonp&pn=$page&type=1&oid=$oid&sort=2&_=${System.currentTimeMillis()}",
            OkUtils.headers(biliBiliEntity.cookie, "https://www.bilibili.com/"))
        return if (jsonNode.getInteger("code") == 0) {
            val jsonArray = jsonNode["data"]["replies"]
            val list = mutableListOf<BiliBiliReplay>()
            for (obj in jsonArray) {
                val biliReplay = BiliBiliReplay(obj.getString("rpid"), obj["content"].getString("message"))
                list.add(biliReplay)
            }
            list
        }else listOf()
    }

    suspend fun reportComment(biliBiliEntity: BiliBiliEntity, oid: String, rpId: String, reason: Int): CommonResult<Void> {
        // 违法违规 9   色情  2    低俗 10    赌博诈骗  12
        // 人身攻击  7   侵犯隐私 15
        // 垃圾广告 1   引战 4    剧透   5    刷屏   3      抢楼 16    内容不相关   8     青少年不良信息  17
        //  其他 0
        val map = mapOf("oid" to oid, "type" to "1", "rpid" to rpId, "reason" to reason.toString(),
            "content" to "", "ordering" to "heat", "jsonp" to "jsonp", "csrf" to biliBiliEntity.token)
        val jsonNode = OkHttpKtUtils.postJson("https://api.bilibili.com/x/v2/reply/report", map,
            OkUtils.headers(biliBiliEntity.cookie, "https://www.bilibili.com/"))
        return if (jsonNode.getInteger("code") == 0) CommonResult.success(message = "举报评论成功！！")
        else CommonResult.failure("举报评论失败！！")
    }

    suspend fun getOidByBvId(bvId: String): String {
        val html = OkHttpKtUtils.getStr("https://www.bilibili.com/video/$bvId",
            OkUtils.ua(UA.PC))
        val jsonStr = MyUtils.regex("INITIAL_STATE__=", ";\\(function\\(\\)", html)!!
        val jsonNode = jsonStr.toJsonNode()
        return jsonNode.getString("aid")
    }

    suspend fun followed(biliBiliEntity: BiliBiliEntity): CommonResult<List<BiliBiliFollowed>> {
        val list = mutableListOf<BiliBiliFollowed>()
        var i = 1
        while (true) {
            val jsonNode = onceFollowed(biliBiliEntity, i++)
            if (jsonNode.getInteger("code") == 0) {
                val jsonArray = jsonNode["data"]["list"]
                if (jsonArray.size() == 0) break
                for (any in jsonArray) {
                    list.add(BiliBiliFollowed(any.getString("mid"), any.getString("uname")))
                }
            } else return CommonResult.failure(jsonNode.getString("message"))
        }
        return CommonResult.success(list)
    }

    private suspend fun onceFollowed(biliBiliEntity: BiliBiliEntity, i: Int): JsonNode {
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
    var ipFrom: String = "",
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
    var imageUrl: String = "",
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
