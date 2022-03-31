package me.kuku.yuq.logic

import me.kuku.utils.*
import me.kuku.pojo.Result
import me.kuku.yuq.entity.NetEaseEntity

object NetEaseLogic {

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
        val response = OkHttpUtils.post("https://music.163.com/weapi/login/cellphone", prepare(map))
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




}