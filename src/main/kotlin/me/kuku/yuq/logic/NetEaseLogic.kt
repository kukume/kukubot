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
                list.add(Mission(it.getLong("userMissionId"), it.getInteger("period"), it.getString("description")))
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
                list.add(Mission(it.getLong("userMissionId"), it.getInteger("period"), it.getString("description")))
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


}

data class Mission(val userMissionId: Long?, val period: Int, val description: String = "")