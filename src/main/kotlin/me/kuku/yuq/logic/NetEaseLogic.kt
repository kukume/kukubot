@file:Suppress("DuplicatedCode")

package me.kuku.yuq.logic

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import me.kuku.utils.*
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.yuq.entity.NetEaseEntity

object NetEaseLogic {

    private const val domain = "https://music.163.com"

    private fun aesEncode(secretData: String, secret: String): String {
        val vi = "0102030405060708"
        return AESUtils.encrypt(secretData, secret, vi)!!
    }

    private fun prepare(map: Map<String, String>): Map<String, String> {
        val nonce = "0CoJUm6Qyw8W8jud"
        val secretKey = "TA3YiYCfY2dDJQgg"
        val encSecKey =
            "84ca47bca10bad09a6b04c5c927ef077d9b9f1e37098aa3eac6ea70eb59df0aa28b691b7e75e4f1f9831754919ea784c8f74fbfadf2898b0be17849fd656060162857830e241aba44991601f137624094c114ea8d17bce815b0cd4e5b8e2fbaba978c6d1d14dc3d1faf852bdd28818031ccdaaa13a6018e1024e2aae98844210"
        var param = aesEncode(map.toJSONString(), nonce)
        param = aesEncode(param, secretKey)
        return mapOf("params" to param, "encSecKey" to encSecKey)
    }

    fun login(phone: String, password: String): Result<NetEaseEntity> {
        val map = mapOf("checkToken" to "9ca17ae2e6ffcda170e2e6ee8dd53df59ab694c65efcbc8fa6d55b938f8faaf17eedbaf783d944ac8aa494bb2af0feaec3b92a9699a392aa61fc9e9c95c55b938f9aa7d44a8fafbf96ce7caf8b9893b85be994ee9e",
            "countrycode" to "86", "password" to if (password.length == 32) password else password.md5(), "phone" to phone,
            "rememberLogin" to "true")
        val response = OkHttpUtils.post("$domain/weapi/login/cellphone", prepare(map))
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

    fun sign(netEaseEntity: NetEaseEntity): Result<Void> {
        val map = mapOf("type" to "1")
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/point/dailyTask", prepare(map),
            OkUtils.cookie(netEaseEntity.cookie()))
        val code = jsonObject.getInteger("code")
        return if (code == 200 || code == -2) Result.success()
        else Result.failure(jsonObject.getString("msg"))
    }

    private fun recommend(netEaseEntity: NetEaseEntity): Result<MutableList<String>> {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/v1/discovery/recommend/resource",
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


    private fun songId(playListId: String): JSONArray {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/v3/playlist/detail",
            prepare(mapOf("id" to playListId, "total" to "true", "limit" to "1000", "n" to "1000"))
        )
        return jsonObject.getJSONObject("playlist").getJSONArray("trackIds")
    }

    fun listenMusic(netEaseEntity: NetEaseEntity): Result<Void> {
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
            val jsonObject = OkHttpUtils.postJson("$domain/weapi/feedback/weblog", prepare(mapOf("logs" to ids.toString())),
                OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
            if (jsonObject.getInteger("code") == 200) Result.success()
            else Result.failure(jsonObject.getString("message"))
        } else Result.failure(recommend.message)
    }

    fun musicianStageMission(netEaseEntity: NetEaseEntity): Result<MutableList<Mission>>? {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/nmusician/workbench/mission/stage/list", prepare(mapOf()),
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

    fun musicianCycleMission(netEaseEntity: NetEaseEntity): Result<List<Mission>> {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/nmusician/workbench/mission/cycle/list",
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

    fun musicianReceive(netEaseEntity: NetEaseEntity, mission: Mission): Result<Void> {
        val missionId = mission.userMissionId?.toString() ?: return Result.failure("userMissionId为空")
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/nmusician/workbench/mission/reward/obtain/new",
            prepare(mapOf("userMissionId" to missionId, "period" to mission.period.toString())),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        val code = jsonObject.getInteger("code")
        return if (code == 200 || code == -2) Result.success()
        else Result.failure(jsonObject.getString("msg"))
    }

    fun userAccess(netEaseEntity: NetEaseEntity): Result<Void> {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/creator/user/access", prepare(mapOf()), OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC))
        return if (jsonObject.getInteger("code") == 200) Result.success()
        else Result.failure(jsonObject.getString("msg"))
    }

    fun musicianSign(netEaseEntity: NetEaseEntity): Result<Void> {
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

    fun myMusic(netEaseEntity: NetEaseEntity): Result<List<NetEaseSong>> {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/nmusician/production/common/artist/song/item/list/get?csrf_token=${netEaseEntity.csrf}",
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

    fun personalizedPlaylist(netEaseEntity: NetEaseEntity): Result<List<Play>> {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/personalized/playlist",
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

    fun shareResource(netEaseEntity: NetEaseEntity, id: Long, msg: String = "每日分享"): Result<Long> {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/share/friends/resource",
            prepare(mapOf("type" to "playlist", "id" to id.toString(), "msg" to msg)),
            mapOf("cookie" to netEaseEntity.cookie(), "referer" to domain, "user-agent" to UA.PC.value)
        )
        return if (jsonObject.getInteger("code") == 200)
            Result.success("成功", jsonObject.getLong("id"))
        else Result.failure(jsonObject.getString("message"))
    }

    fun removeDy(netEaseEntity: NetEaseEntity, id: Long): Result<Void> {
        val jsonObject = OkHttpUtils.postJson("$domain/weapi/event/delete",
            prepare(mapOf("id" to id.toString())),
            OkUtils.headers(netEaseEntity.cookie(), domain, UA.PC)
        )
        return if (jsonObject.getInteger("code") == 200) Result.success()
        else Result.failure(jsonObject.getString("message") ?: "删除动态失败")
    }

    fun publish(netEaseEntity: NetEaseEntity): Result<Void> {
        val result = personalizedPlaylist(netEaseEntity)
        if (result.isFailure) return Result.failure(result.message)
        val play = result.data.random()
        val res = shareResource(netEaseEntity, play.id)
        return if (res.isSuccess) {
            val id = res.data
            removeDy(netEaseEntity, id)
            val missionResult = musicianCycleMission(netEaseEntity)
            return if (result.isSuccess) {
                val list = missionResult.data
                for (mission in list) {
                    if (mission.description == "发布动态") {
                        if (mission.type != 100) {
                            userAccess(netEaseEntity)
                        }
                        return musicianReceive(netEaseEntity, mission)
                    }
                }
                Result.failure("没有找到音乐人签到任务")
            } else Result.failure(result.message)
        }
        else Result.failure(res.message)
    }


}

data class Mission(val userMissionId: Long?, val period: Int, val type: Int, val description: String = "")

data class NetEaseSong(val songName: String, val songId: Long, val albumId: Long, val albumName: String)

data class Play(val name: String, val id: Long, val playCount: Long)