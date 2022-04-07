@file:Suppress("DuplicatedCode")

package me.kuku.yuq.logic

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import me.kuku.utils.*
import me.kuku.pojo.Result
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
    }

    suspend fun login(phone: String, password: String): Result<NetEaseEntity> {
        val map = mapOf("checkToken" to "9ca17ae2e6ffcda170e2e6ee8dd53df59ab694c65efcbc8fa6d55b938f8faaf17eedbaf783d944ac8aa494bb2af0feaec3b92a9699a392aa61fc9e9c95c55b938f9aa7d44a8fafbf96ce7caf8b9893b85be994ee9e",
            "countrycode" to "86", "password" to if (password.length == 32) password else password.md5(), "phone" to phone,
            "rememberLogin" to "true")
        val response = OkHttpKtUtils.post("$domain/weapi/login/cellphone", prepare(map))
        val jsonObject = OkUtils.json(response)
        return if (jsonObject.getInteger("code") == 200) {
            val cookie = OkUtils.cookie(response)
            val csrf = OkUtils.cookie(cookie, "__csrf")
            val musicU = OkUtils.cookie(cookie, "MUSIC_U")
            Result.success(NetEaseEntity().also {
                it.csrf = csrf!!
                it.musicU = musicU!!
            })
        } else Result.failure(jsonObject.getString("msg"))
    }

    suspend fun sign(netEaseEntity: NetEaseEntity): Result<Void> {
        val map = mapOf("type" to "1")
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/point/dailyTask", prepare(map),
            OkUtils.cookie(netEaseEntity.cookie()))
        val code = jsonObject.getInteger("code")
        return if (code == 200 || code == -2) Result.success()
        else Result.failure(jsonObject.getString("msg"))
    }

    private suspend fun recommend(netEaseEntity: NetEaseEntity): Result<MutableList<String>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/v1/discovery/recommend/resource",
            prepare(mapOf("csrf_token" to netEaseEntity.csrf)), OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return when (jsonObject.getInteger("code")) {
            200 -> {
                val jsonArray = jsonObject.getJSONArray("recommend")
                val list = mutableListOf<String>()
                jsonArray.map { it as JSONObject }.forEach { list.add(it.getString("id")) }
                return Result.success(list)
            }
            301 -> Result.failure("您的网易云音乐cookie已失效", null)
            else -> Result.failure(jsonObject.getString("msg"))
        }
    }


    private suspend fun songId(playListId: String): JSONArray {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/v3/playlist/detail",
            prepare(mapOf("id" to playListId, "total" to "true", "limit" to "1000", "n" to "1000"))
        )
        return jsonObject.getJSONObject("playlist").getJSONArray("trackIds")
    }

    suspend fun listenMusic(netEaseEntity: NetEaseEntity): Result<Void> {
        val recommend = recommend(netEaseEntity)
        return if (recommend.isSuccess) {
            val playList = recommend.data
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
            if (jsonObject.getInteger("code") == 200) Result.success()
            else Result.failure(jsonObject.getString("message"))
        } else Result.failure(recommend.message)
    }

    suspend fun musicianStageMission(netEaseEntity: NetEaseEntity): Result<MutableList<Mission>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nmusician/workbench/mission/stage/list", prepare(mapOf()),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
        return if (jsonObject.getInteger("code") == 200) {
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("list")
            val list = mutableListOf<Mission>()
            jsonArray.map { it as JSONObject }.forEach {
                list.add(Mission(it.getLong("userMissionId"), it.getInteger("period"), it.getInteger("type"), it.getString("description")))
            }
            Result.success(list)
        } else Result.failure(jsonObject.getString("msg"))
    }

    suspend fun musicianCycleMission(netEaseEntity: NetEaseEntity): Result<List<Mission>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nmusician/workbench/mission/cycle/list",
            prepare(mapOf("actionType" to "", "platform" to "")),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
        return if (jsonObject.getInteger("code") == 200) {
            val jsonArray = jsonObject.getJSONObject("data").getJSONArray("list")
            val list = mutableListOf<Mission>()
            jsonArray.map { it as JSONObject }.forEach {
                list.add(Mission(it.getLong("userMissionId"), it.getInteger("period"), it.getInteger("type"), it.getString("description")))
            }
            Result.success(list)
        } else Result.failure(jsonObject.getString("msg"))
    }

    suspend fun musicianReceive(netEaseEntity: NetEaseEntity, mission: Mission): Result<Void> {
        val missionId = mission.userMissionId?.toString() ?: return Result.failure("userMissionId为空")
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nmusician/workbench/mission/reward/obtain/new",
            prepare(mapOf("userMissionId" to missionId, "period" to mission.period.toString())),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        val code = jsonObject.getInteger("code")
        return if (code == 200 || code == -2) Result.success()
        else Result.failure(jsonObject.getString("msg"))
    }

    suspend fun userAccess(netEaseEntity: NetEaseEntity): Result<Void> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/creator/user/access", prepare(mapOf()), OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
        return if (jsonObject.getInteger("code") == 200) Result.success()
        else Result.failure(jsonObject.getString("msg"))
    }

    suspend fun musicianSign(netEaseEntity: NetEaseEntity): Result<Void> {
        val result = musicianCycleMission(netEaseEntity)
        return if (result.isSuccess) {
            val list = result.data
            for (mission in list) {
                if (mission.description == "音乐人中心签到") {
                    if (mission.type != 100) {
                        userAccess(netEaseEntity)
                    }
                    return musicianReceive(netEaseEntity, mission)
                }
            }
            Result.failure("没有找到音乐人签到任务")
        } else Result.failure(result.message)
    }

    suspend fun myMusic(netEaseEntity: NetEaseEntity): Result<List<NetEaseSong>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/nmusician/production/common/artist/song/item/list/get?csrf_token=${netEaseEntity.csrf}",
            prepare(mapOf("fromBackend" to "0", "limit" to "10", "offset" to "0", "online" to "1")),
            mapOf("user-agent" to UA.PC.value, "cookie" to netEaseEntity.cookie(), "referer" to "https://music.163.com/nmusician/web/albums/work/actor/song/self/pub"))
        return if (jsonObject.getInteger("code") == 200) {
            val list = mutableListOf<NetEaseSong>()
            jsonObject.getJSONObject("data").getJSONArray("list").map { it as JSONObject }.forEach {
                list.add(NetEaseSong(it.getString("songName"), it.getLong("songId"), it.getLong("albumId"), it.getString("albumName")))
            }
            Result.success(list)
        } else Result.failure(jsonObject.getString("message"))
    }

    suspend fun personalizedPlaylist(netEaseEntity: NetEaseEntity): Result<List<Play>> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/personalized/playlist",
            prepare(mapOf("limit" to "9")), OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) {
            val list = mutableListOf<Play>()
            jsonObject.getJSONArray("result").map { it as JSONObject }.forEach {
                list.add(Play(it.getString("name"), it.getLong("id"), it.getLong("playCount")))
            }
            Result.success(list)
        } else Result.failure(jsonObject.getString("message") ?: "获取失败")
    }

    suspend fun shareResource(netEaseEntity: NetEaseEntity, id: Long, msg: String = "每日分享"): Result<Long> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/share/friends/resource",
            prepare(mapOf("type" to "playlist", "id" to id.toString(), "msg" to msg)),
            mapOf("cookie" to netEaseEntity.cookie(), "referer" to domain, "user-agent" to UA.PC.value)
        )
        return if (jsonObject.getInteger("code") == 200)
            Result.success("成功", jsonObject.getLong("id"))
        else Result.failure(jsonObject.getString("message"))
    }

    suspend fun removeDy(netEaseEntity: NetEaseEntity, id: Long): Result<Void> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/event/delete",
            prepare(mapOf("id" to id.toString())),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) Result.success()
        else Result.failure(jsonObject.getString("message") ?: "删除动态失败")
    }

    private suspend fun finishCycleMission(netEaseEntity: NetEaseEntity, name: String): Result<Void> {
        val missionResult = musicianCycleMission(netEaseEntity)
        return if (missionResult.isSuccess) {
            val list = missionResult.data
            for (mission in list) {
                if (mission.description == name) {
                    if (mission.type != 100) {
                        userAccess(netEaseEntity)
                    }
                    return musicianReceive(netEaseEntity, mission)
                }
            }
            Result.failure("没有找到音乐人签到任务")
        } else Result.failure(missionResult.message)
    }

    private suspend fun finishStageMission(netEaseEntity: NetEaseEntity, name: String): Result<Void> {
        val missionResult = musicianStageMission(netEaseEntity)
        return if (missionResult.isSuccess) {
            val list = missionResult.data
            for (mission in list) {
                if (mission.description == name) {
                    if (mission.type != 100) {
                        userAccess(netEaseEntity)
                    }
                    return musicianReceive(netEaseEntity, mission)
                }
            }
            Result.failure("没有找到音乐人签到任务")
        } else Result.failure(missionResult.message)
    }

    suspend fun publish(netEaseEntity: NetEaseEntity): Result<Void> {
        val result = personalizedPlaylist(netEaseEntity)
        if (result.isFailure) return Result.failure(result.message)
        val play = result.data.random()
        val res = shareResource(netEaseEntity, play.id)
        return if (res.isSuccess) {
            val id = res.data
            removeDy(netEaseEntity, id)
            finishCycleMission(netEaseEntity, "发布动态")
        }
        else Result.failure(res.message)
    }

    suspend fun mLogNosToken(netEaseEntity: NetEaseEntity, url: String): Result<MLogInfo> {
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
            Result.success(MLogInfo(dataJsonObject.getLong("resourceId"), dataJsonObject.getString("objectKey"), dataJsonObject.getString("token"),
                dataJsonObject.getString("bucket"), bytes))
        } else Result.failure(jsonObject.getString("message"))
    }

    suspend fun uploadFile(netEaseEntity: NetEaseEntity, mLogInfo: MLogInfo): UploadFileInfo {
        val url = "http://45.127.129.8/${mLogInfo.bucket}/${mLogInfo.objectKey}?offset=0&complete=true&version=1.0"
        val contentType = "image/jpg"
        val jsonObject = OkHttpKtUtils.postJson(url, OkUtils.streamBody(mLogInfo.byteArray, contentType),
            mapOf("x-nos-token" to mLogInfo.token, "cookie" to netEaseEntity.cookie(), "referer" to domain))
        return jsonObject.toJavaObject(UploadFileInfo::class.java)
    }

    suspend fun songDetail(netEaseEntity: NetEaseEntity, id: Long): Result<SongDetail> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/v3/song/detail",
            prepare("""
                {"c": "[{\"id\": $id}]", "ids": "[$id]"}
            """.trimIndent(), netEaseEntity),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) {
            val songJsonObject = jsonObject.getJSONArray("songs").getJSONObject(0)
            Result.success(SongDetail(songJsonObject.getString("name"), songJsonObject.getJSONArray("ar").getJSONObject(0).getString("name"),
                songJsonObject.getJSONObject("al").getString("picUrl") + "?param=500y500"))
        } else Result.failure(jsonObject.getString("message"))
    }

    suspend fun publishMLog(netEaseEntity: NetEaseEntity): Result<Void> {
        val musicResult = myMusic(netEaseEntity)
        val songId = musicResult.data.random().songId
        val songDetailResult = songDetail(netEaseEntity, songId)
        val songDetail = songDetailResult.data
        val infoResult = mLogNosToken(netEaseEntity, songDetail.pic)
        if (infoResult.isFailure) return Result.failure(infoResult.message)
        val mLogInfo = infoResult.data
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
        } else Result.failure(jsonObject.getString("message"))
    }

    suspend fun musicComment(netEaseEntity: NetEaseEntity, id: Long, comment: String = "欢迎大家收听"): Result<Long> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/v1/resource/comments/add",
            prepare(mapOf("threadId" to "R_SO_4_$id", "content" to comment)),
            OkUtils.headers(netEaseEntity.cookie().replace("os=pc; ", "") + "os=android; ", domain, ua)
        )
        return if (jsonObject.getInteger("code") == 200) {
            Result.success(jsonObject.getJSONObject("comment").getLong("commentId"))
        } else Result.failure(jsonObject.getString("message"))
    }

    suspend fun deleteMusicComment(netEaseEntity: NetEaseEntity, id: Long, commentId: Long): Result<Void> {
        val jsonObject = OkHttpKtUtils.postJson("$domain/weapi/resource/comments/delete",
            prepare(mapOf("commentId" to commentId.toString(), "threadId" to "R_SO_4_$id")),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) Result.success()
        else Result.failure(jsonObject.getString("message"))
    }

    suspend fun myMusicComment(netEaseEntity: NetEaseEntity): Result<Void> {
        val musicResult = myMusic(netEaseEntity)
        if (musicResult.isFailure) return Result.failure(musicResult.message)
        val netEaseSong = musicResult.data.random()
        val result = musicComment(netEaseEntity, netEaseSong.songId)
        val commentId = result.data
        return deleteMusicComment(netEaseEntity, netEaseSong.songId, commentId).also {
            finishStageMission(netEaseEntity, "发布主创说")
        }
    }

}

data class Mission(val userMissionId: Long?, val period: Int, val type: Int, val description: String = "")

data class NetEaseSong(val songName: String, val songId: Long, val albumId: Long, val albumName: String)

data class Play(val name: String, val id: Long, val playCount: Long)

data class MLogInfo(val resourceId: Long, val objectKey: String, val token: String, val bucket: String, val byteArray: ByteArray)

data class UploadFileInfo(val callbackRetMsg: String, val offset: Long, val requestId: String, val context: String)

data class SongDetail(val name: String, val artistName: String, val pic: String)