package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.WeiboPojo

@AutoBind
interface WeiboLogic {
    fun hotSearch(): String
    fun getIdByName(name: String): CommonResult<List<WeiboPojo>>
    fun convertStr(weiboPojo: WeiboPojo): String
    fun getWeiboById(id: String): CommonResult<List<WeiboPojo>>
    fun login(username: String, password: String): CommonResult<MutableMap<String, String>>
    fun loginBySms(token: String, phone: String, code: String): CommonResult<WeiboEntity>
    fun getFriendWeibo(weiboEntity: WeiboEntity): CommonResult<List<WeiboPojo>>
}