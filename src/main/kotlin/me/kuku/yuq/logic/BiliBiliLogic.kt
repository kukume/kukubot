package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.BiliBiliEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.entity.WeiboEntity
import me.kuku.yuq.pojo.BiliBiliPojo
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface BiliBiliLogic {
    fun getIdByName(username: String): CommonResult<List<BiliBiliPojo>>
    fun convertStr(biliBiliPojo: BiliBiliPojo): String
    fun getDynamicById(id: String): CommonResult<List<BiliBiliPojo>>
    fun loginByQQ(qqEntity: QQEntity): CommonResult<BiliBiliEntity>
    fun loginByWeibo(weiboEntity: WeiboEntity): CommonResult<BiliBiliEntity>
    fun getFriendDynamic(biliBiliEntity: BiliBiliEntity): CommonResult<List<BiliBiliPojo>>
    fun isLiveOnline(id: String): Boolean
    fun liveSign(biliBiliEntity: BiliBiliEntity): String
    fun like(biliBiliEntity: BiliBiliEntity, id: String, isLike: Boolean): String
    fun comment(biliBiliEntity: BiliBiliEntity, id: String, type: String, content: String): String
    fun forward(biliBiliEntity: BiliBiliEntity, id: String, content: String): String
    fun tossCoin(biliBiliEntity: BiliBiliEntity, rid: String, count: Int): String
    fun favorites(biliBiliEntity: BiliBiliEntity, rid: String, name: String): String
}