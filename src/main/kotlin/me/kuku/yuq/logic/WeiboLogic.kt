package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.WeiboPojo

@AutoBind
interface WeiboLogic {
    fun hotSearch(): List<String>
    fun getIdByName(name: String): CommonResult<List<WeiboPojo>>
    fun convertStr(weiboPojo: WeiboPojo): String
    fun getWeiboById(id: String): CommonResult<List<WeiboPojo>>
    fun getCaptchaUrl(pcId: String): String
    fun preparedLogin(username: String, password: String): CommonResult<MutableMap<String, String>>
    fun login(map: MutableMap<String, String>, door: String?): CommonResult<MutableMap<String, String>>
    fun loginSuccess(cookie: String, referer: String, url: String): WeiboEntity
    fun loginBySms(token: String, phone: String, code: String): CommonResult<WeiboEntity>
    fun loginByQQ(qqEntity: QQEntity): CommonResult<WeiboEntity>
    fun getFriendWeibo(weiboEntity: WeiboEntity): CommonResult<List<WeiboPojo>>
    fun getMyWeibo(weiboEntity: WeiboEntity): CommonResult<List<WeiboPojo>>
    fun weiboTopic(keyword: String): CommonResult<List<WeiboPojo>>
    fun like(weiboEntity: WeiboEntity, id: String): String
    fun comment(weiboEntity: WeiboEntity, id: String, commentContent: String): String
    fun forward(weiboEntity: WeiboEntity, id: String, content: String, picUrl: String?): String
    fun getUserInfo(id: String): String
    fun publishWeibo(weiboEntity: WeiboEntity, content: String, url: List<String>?): String
    fun removeWeibo(weiboEntity: WeiboEntity, id: String): String
    fun favoritesWeibo(weiboEntity: WeiboEntity, id: String): String
}