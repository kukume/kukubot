@file:Suppress("DuplicatedCode")

package me.kuku.yuq.logic

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import me.kuku.utils.*
import me.kuku.pojo.CommonResult
import me.kuku.pojo.UA
import me.kuku.yuq.entity.NetEaseEntity
import okhttp3.internal.toHexString

object NetEaseLogic {

    private const val domain = "https://music.163.com"

    private const val ua = "NeteaseMusic/8.7.22.220331222744(8007022);Dalvik/2.1.0 (Linux; U; Android 12; M2007J3SC Build/SKQ1.211006.001)"

    private fun aesEncode(secretData: String, secret: String): String {
        val vi = "0102030405060708"
        return AESUtils.encrypt(secretData, secret, vi)!!
    }

    private fun prepare(map: Map<String, String>, netEaseEntity: NetEaseEntity? = null): Map<String, String> {
        return prepare(map.toJSONString(), netEaseEntity)
    }

    private fun prepare(json: String, netEaseEntity: NetEaseEntity? = null): Map<String, String> {
        val nonce = "0CoJUm6Qyw8W8jud"
        val secretKey = "TA3YiYCfY2dDJQgg"
        val encSecKey =
            "84ca47bca10bad09a6b04c5c927ef077d9b9f1e37098aa3eac6ea70eb59df0aa28b691b7e75e4f1f9831754919ea784c8f74fbfadf2898b0be17849fd656060162857830e241aba44991601f137624094c114ea8d17bce815b0cd4e5b8e2fbaba978c6d1d14dc3d1faf852bdd28818031ccdaaa13a6018e1024e2aae98844210"
        val jsonObject = JSON.parseObject(json)
        netEaseEntity?.let {
            jsonObject["csrf_token"] = netEaseEntity.csrf
        }
        var param = aesEncode(jsonObject.toString(), nonce)
        param = aesEncode(param, secretKey)
        return mapOf("params" to param, "encSecKey" to encSecKey)
//        val nonce = "0CoJUm6Qyw8W8jud"
//        val secretKey = MyUtils.randomLetterLowerNum(16).toByteArray().hex().substring(0, 16)
//        val ss = BigInteger(HexUtils.byteArrayToHex(secretKey.reversed().toByteArray()), 16)
//            .pow("010001".toInt(16))
//        val sss = BigInteger("00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7", 16)
//        val d = ss.divideAndRemainder(sss)[1]
//        val encSecKey = d.toString(16)
//        val jsonObject = JSON.parseObject(json)
//        netEaseEntity?.let {
//            jsonObject["csrf_token"] = netEaseEntity.csrf
//        }
//        var param = aesEncode(jsonObject.toString(), nonce)
//        param = aesEncode(param, secretKey)
//        return mapOf("params" to param, "encSecKey" to encSecKey)
    }

    suspend fun login(phone: String, password: String): CommonResult<NetEaseEntity> {
        val map = mapOf("countrycode" to "86", "password" to if (password.length == 32) password else password.md5(), "phone" to phone,
            "rememberLogin" to "true")
        val response = OkHttpKtUtils.post("$domain/weapi/login/cellphone", prepare(map),
            mapOf("crypto" to "weapi",
                "Referer" to "https://music.163.com", "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36"))
        val jsonObject = OkUtils.json(response)
        return if (jsonObject.getInteger("code") == 200) {
            val cookie = OkUtils.cookie(response)
            val csrf = OkUtils.cookie(cookie, "__csrf")
            val musicU = OkUtils.cookie(cookie, "MUSIC_U")
            CommonResult.success(NetEaseEntity().also {
                it.csrf = csrf!!
                it.musicU = musicU!!
            })
        } else CommonResult.failure(jsonObject.getString("message"))
    }

    suspend fun qrcode(): String {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/login/qrcode/unikey", prepare("""{"type": 1}"""))
        return jsonObject.getString("unikey")
    }

    suspend fun checkQrcode(key: String): CommonResult<NetEaseEntity> {
        val response = OkHttpKtUtils.post("https://music.163.com/weapi/login/qrcode/client/login", prepare("""{"type":1,"key":"$key"}"""), mapOf("crypto" to "weapi"))
        val jsonObject = OkUtils.json(response)
        return when (jsonObject.getInteger("code")) {
            803 -> {
                val cookie = OkUtils.cookie(response)
                val csrf = OkUtils.cookie(cookie, "__csrf")
                val musicU = OkUtils.cookie(cookie, "MUSIC_U")
                CommonResult.success(NetEaseEntity().also {
                    it.csrf = csrf!!
                    it.musicU = musicU!!
                })
            }
            801 -> CommonResult.failure(code = 0, message = "等待扫码")
            802 -> CommonResult.failure(code = 1, message = "${jsonObject.getString("nickname")}已扫码，等待确认登陆")
            800 -> CommonResult.failure("二维码已过期")
            else -> CommonResult.failure(jsonObject.getString("message"))
        }
    }

    suspend fun sign(netEaseEntity: NetEaseEntity): CommonResult<Void> {
        val map = mapOf("type" to "1")
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/point/dailyTask", prepare(map),
            OkUtils.cookie(netEaseEntity.cookie()))
        val code = jsonObject.getInteger("code")
        return if (code == 200 || code == -2) CommonResult.success()
        else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun recommend(netEaseEntity: NetEaseEntity): CommonResult<MutableList<String>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/v1/discovery/recommend/resource",
            prepare(mapOf("csrf_token" to netEaseEntity.csrf)), OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return when (jsonObject.getInteger("code")) {
            200 -> {
                val jsonArray = jsonObject.getJSONArray("recommend")
                val list = mutableListOf<String>()
                jsonArray.map { it as JSONObject }.forEach { list.add(it.getString("id")) }
                return CommonResult.success(list)
            }
            301 -> CommonResult.failure("您的网易云音乐cookie已失效", null)
            else -> CommonResult.failure(jsonObject.getString("message"))
        }
    }


    private suspend fun songId(playListId: String): JSONArray {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/v3/playlist/detail",
            prepare(mapOf("id" to playListId, "total" to "true", "limit" to "1000", "n" to "1000"))
        )
        return jsonObject.getJSONObject("playlist").getJSONArray("trackIds")
    }

    suspend fun listenMusic(netEaseEntity: NetEaseEntity): CommonResult<Void> {
        val recommend = recommend(netEaseEntity)
        return if (recommend.success()) {
            val playList = recommend.data()
            val ids = JSONArray()
            while (ids.size < 310) {
                val songIds = songId(playList.random())
                var k = 0
                while (ids.size < 310 && k < songIds.size) {
                    val jsonObject = JSONObject()
                    jsonObject["download"] = 0
                    jsonObject["end"] = "playend"
                    jsonObject["id"] = songIds.getJSONObject(k).getInteger("id")
                    jsonObject["sourceId"] = ""
                    jsonObject["time"] = 240
                    jsonObject["type"] = "song"
                    jsonObject["wifi"] = "0"
                    val totalJsonObject = JSONObject()
                    totalJsonObject["json"] = jsonObject
                    totalJsonObject["action"] = "play"
                    ids.add(totalJsonObject)
                    k++
                }
            }
            val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/feedback/weblog", prepare(mapOf("logs" to ids.toString())),
                OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
            if (jsonObject.getInteger("code") == 200) CommonResult.success()
            else CommonResult.failure(jsonObject.getString("message"))
        } else CommonResult.failure(recommend.message)
    }

    private suspend fun musicianStageMission(netEaseEntity: NetEaseEntity): CommonResult<MutableList<Mission>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nmusician/workbench/mission/stage/list", prepare(mapOf()),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
        return if (jsonObject.getInteger("code") == 200) {
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("list")
            val list = mutableListOf<Mission>()
            jsonArray.map { it as JSONObject }.forEach {
                list.add(Mission(it.getLong("userMissionId"), it.getInteger("period"), it.getInteger("type"), it.getString("description")))
            }
            CommonResult.success(list)
        } else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun musicianCycleMission(netEaseEntity: NetEaseEntity): CommonResult<List<Mission>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nmusician/workbench/mission/cycle/list",
            prepare(mapOf("actionType" to "", "platform" to "")),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
        return if (jsonObject.getInteger("code") == 200) {
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("list")
            val list = mutableListOf<Mission>()
            jsonArray.map { it as JSONObject }.forEach {
                list.add(Mission(it.getLong("userMissionId"), it.getInteger("period"), it.getInteger("type"), it.getString("description")))
            }
            CommonResult.success(list)
        } else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun musicianReceive(netEaseEntity: NetEaseEntity, mission: Mission): CommonResult<Void> {
        val missionId = mission.userMissionId?.toString() ?: return CommonResult.failure("userMissionId为空")
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nmusician/workbench/mission/reward/obtain/new",
            prepare(mapOf("userMissionId" to missionId, "period" to mission.period.toString())),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        val code = jsonObject.getInteger("code")
        return if (code == 200 || code == -2) CommonResult.success()
        else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun userAccess(netEaseEntity: NetEaseEntity): CommonResult<Void> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/creator/user/access", prepare(mapOf()), OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
        return if (jsonObject.getInteger("code") == 200) CommonResult.success()
        else CommonResult.failure(jsonObject.getString("message"))
    }

    suspend fun musicianSign(netEaseEntity: NetEaseEntity): CommonResult<Void> {
        val result = musicianCycleMission(netEaseEntity)
        return if (result.success()) {
            val list = result.data()
            for (mission in list) {
                if (mission.description == "音乐人中心签到") {
                    if (mission.type != 100) {
                        userAccess(netEaseEntity)
                    }
                    return musicianReceive(netEaseEntity, mission)
                }
            }
            CommonResult.failure("没有找到音乐人签到任务")
        } else CommonResult.failure(result.message)
    }

    private suspend fun myMusic(netEaseEntity: NetEaseEntity): CommonResult<List<NetEaseSong>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nmusician/production/common/artist/song/item/list/get?csrf_token=${netEaseEntity.csrf}",
            prepare(mapOf("fromBackend" to "0", "limit" to "10", "offset" to "0", "online" to "1")),
            mapOf("user-agent" to UA.PC.value, "cookie" to netEaseEntity.cookie(), "referer" to "https://music.163.com/nmusician/web/albums/work/actor/song/self/pub"))
        return if (jsonObject.getInteger("code") == 200) {
            val list = mutableListOf<NetEaseSong>()
            jsonObject.getJSONObject("data").getJSONArray("list").map { it as JSONObject }.forEach {
                list.add(NetEaseSong(it.getString("songName"), it.getLong("songId"), it.getLong("albumId"), it.getString("albumName")))
            }
            CommonResult.success(list)
        } else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun personalizedPlaylist(netEaseEntity: NetEaseEntity): CommonResult<List<Play>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/personalized/playlist",
            prepare(mapOf("limit" to "9")), OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) {
            val list = mutableListOf<Play>()
            jsonObject.getJSONArray("result").map { it as JSONObject }.forEach {
                list.add(Play(it.getString("name"), it.getLong("id"), it.getLong("playCount")))
            }
            CommonResult.success(list)
        } else CommonResult.failure(jsonObject.getString("message") ?: "获取失败")
    }

    private suspend fun shareResource(netEaseEntity: NetEaseEntity, id: Long, message: String = "每日分享"): CommonResult<Long> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/share/friends/resource",
            prepare(mapOf("type" to "playlist", "id" to id.toString(), "message" to message)),
            mapOf("cookie" to netEaseEntity.cookie(), "referer" to domain, "user-agent" to UA.PC.value)
        )
        return if (jsonObject.getInteger("code") == 200)
            CommonResult.success(message = "成功", data = jsonObject.getLong("id"))
        else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun removeDy(netEaseEntity: NetEaseEntity, id: Long): CommonResult<Void> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/event/delete",
            prepare(mapOf("id" to id.toString())),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) CommonResult.success()
        else CommonResult.failure(jsonObject.getString("message") ?: "删除动态失败")
    }

    private suspend fun finishCycleMission(netEaseEntity: NetEaseEntity, name: String): CommonResult<Void> {
        val missionCommonResult = musicianCycleMission(netEaseEntity)
        return if (missionCommonResult.success()) {
            val list = missionCommonResult.data()
            for (mission in list) {
                if (mission.description == name) {
                    if (mission.type != 100) {
                        userAccess(netEaseEntity)
                    }
                    return musicianReceive(netEaseEntity, mission)
                }
            }
            CommonResult.failure("没有找到音乐人签到任务")
        } else CommonResult.failure(missionCommonResult.message)
    }

    private suspend fun finishStageMission(netEaseEntity: NetEaseEntity, name: String): CommonResult<Void> {
        val missionCommonResult = musicianStageMission(netEaseEntity)
        return if (missionCommonResult.success()) {
            val list = missionCommonResult.data()
            for (mission in list) {
                if (mission.description == name) {
                    if (mission.type != 100) {
                        userAccess(netEaseEntity)
                    }
                    return musicianReceive(netEaseEntity, mission)
                }
            }
            CommonResult.failure("没有找到音乐人签到任务")
        } else CommonResult.failure(missionCommonResult.message)
    }

    suspend fun publish(netEaseEntity: NetEaseEntity): CommonResult<Void> {
        val result = personalizedPlaylist(netEaseEntity)
        if (result.failure()) return CommonResult.failure(result.message)
        val play = result.data().random()
        val res = shareResource(netEaseEntity, play.id)
        return if (res.success()) {
            val id = res.data()
            removeDy(netEaseEntity, id)
            finishCycleMission(netEaseEntity, "发布动态")
        }
        else CommonResult.failure(res.message)
    }

    private suspend fun mLogNosToken(netEaseEntity: NetEaseEntity, url: String): CommonResult<MLogInfo> {
        val bizKey = StringBuilder()
        for (i in 0..8) {
            bizKey.append(MyUtils.randomInt(0, 15).toHexString().replace("0x", ""))
        }
        val fileName = "album.jpg"
        val bytes =
            OkHttpKtUtils.getBytes(url)
        val size = bytes.size
        val md5 = bytes.md5()
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nos/token/whalealloc",
            prepare(mapOf("bizKey" to bizKey.toString(), "filename" to fileName, "bucket" to "yyimgs",
                "md5" to md5, "type" to "image", "fileSize" to size.toString())),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) {
            val dataJsonObject = jsonObject.getJSONObject("data")
            CommonResult.success(MLogInfo(dataJsonObject.getLong("resourceId"), dataJsonObject.getString("objectKey"), dataJsonObject.getString("token"),
                dataJsonObject.getString("bucket"), bytes))
        } else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun uploadFile(netEaseEntity: NetEaseEntity, mLogInfo: MLogInfo): UploadFileInfo {
        val url = "http://45.127.129.8/${mLogInfo.bucket}/${mLogInfo.objectKey}?offset=0&complete=true&version=1.0"
        val contentType = "image/jpg"
        val jsonObject = OkHttpKtUtils.postJson(url, OkUtils.streamBody(mLogInfo.byteArray, contentType),
            mapOf("x-nos-token" to mLogInfo.token, "cookie" to netEaseEntity.cookie(), "referer" to domain))
        return jsonObject.toJavaObject(UploadFileInfo::class.java)
    }

    private suspend fun songDetail(netEaseEntity: NetEaseEntity, id: Long): CommonResult<SongDetail> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/v3/song/detail",
            prepare("""
                {"c": "[{\"id\": $id}]", "ids": "[$id]"}
            """.trimIndent(), netEaseEntity),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) {
            val songJsonObject = jsonObject.getJSONArray("songs").getJSONObject(0)
            CommonResult.success(SongDetail(songJsonObject.getString("name"), songJsonObject.getJSONArray("ar").getJSONObject(0).getString("name"),
                songJsonObject.getJSONObject("al").getString("picUrl") + "?param=500y500"))
        } else CommonResult.failure(jsonObject.getString("message"))
    }

    suspend fun publishMLog(netEaseEntity: NetEaseEntity): CommonResult<Void> {
        val musicCommonResult = myMusic(netEaseEntity)
        if (musicCommonResult.failure()) return CommonResult.failure("获取您的歌曲失败，可能您没有歌曲或者cookie已失效")
        val songId = musicCommonResult.data().random().songId
        val songDetailCommonResult = songDetail(netEaseEntity, songId)
        val songDetail = songDetailCommonResult.data()
        val infoCommonResult = mLogNosToken(netEaseEntity, songDetail.pic)
        if (infoCommonResult.failure()) return CommonResult.failure(infoCommonResult.message)
        val mLogInfo = infoCommonResult.data()
        uploadFile(netEaseEntity, mLogInfo)
        val songName = songDetail.name
        val text = "分享${songDetail.artistName}的歌曲: ${songDetail.name}"
        val jsonStr = """
            {"type":1,"mlog":"{\"content\": {\"image\": [{\"height\": 500, \"width\": 500, \"more\": false, \"nosKey\": \"${mLogInfo.bucket}/${mLogInfo.objectKey}\", \"picKey\": ${mLogInfo.resourceId}}], \"needAudio\": false, \"song\": {\"endTime\": 0, \"name\": \"${songName}\", \"songId\": $songId, \"startTime\": 30000}, \"text\": \"$text\"}, \"from\": 0, \"type\": 1}"}
        """.trimIndent()
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/mlog/publish/v1", prepare(jsonStr), OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
        return if (jsonObject.getInteger("code") == 200) {
            val resourceId = jsonObject.getJSONObject("data").getJSONObject("event").getJSONObject("info").getLong("resourceId")
            removeDy(netEaseEntity, resourceId)
            return finishCycleMission(netEaseEntity, "发布mlog")
        } else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun musicComment(netEaseEntity: NetEaseEntity, id: Long, comment: String = "欢迎大家收听"): CommonResult<Long> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/v1/resource/comments/add",
            prepare(mapOf("threadId" to "R_SO_4_$id", "content" to comment)),
            OkUtils.headers(netEaseEntity.cookie().replace("os=pc; ", "") + "os=android; ", domain, ua)
        )
        return if (jsonObject.getInteger("code") == 200) {
            CommonResult.success(jsonObject.getJSONObject("comment").getLong("commentId"))
        } else CommonResult.failure(jsonObject.getString("message"))
    }

    private suspend fun deleteMusicComment(netEaseEntity: NetEaseEntity, id: Long, commentId: Long): CommonResult<Void> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/resource/comments/delete",
            prepare(mapOf("commentId" to commentId.toString(), "threadId" to "R_SO_4_$id")),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) CommonResult.success()
        else CommonResult.failure(jsonObject.getString("message"))
    }

    suspend fun myMusicComment(netEaseEntity: NetEaseEntity): CommonResult<Void> {
        val musicCommonResult = myMusic(netEaseEntity)
        if (musicCommonResult.failure()) return CommonResult.failure(musicCommonResult.message)
        val netEaseSong = musicCommonResult.data().random()
        val result = musicComment(netEaseEntity, netEaseSong.songId)
        val commentId = result.data()
        return deleteMusicComment(netEaseEntity, netEaseSong.songId, commentId).also {
            finishStageMission(netEaseEntity, "发布主创说")
        }
    }

}

data class Mission(val userMissionId: Long?, val period: Int, val type: Int, val description: String = "")

data class NetEaseSong(val songName: String, val songId: Long, val albumId: Long, val albumName: String)

data class Play(val name: String, val id: Long, val playCount: Long)

data class MLogInfo(val resourceId: Long, val objectKey: String, val token: String, val bucket: String, val byteArray: ByteArray)

data class UploadFileInfo(var callbackRetmessage: String = "", var offset: Long = 0, var requestId: String = "", var context: String = "", var callbackRetMsg: String = "")

data class SongDetail(val name: String, val artistName: String, val pic: String)