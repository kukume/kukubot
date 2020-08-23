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
    fun loginByQQ(qqEntity: QQEntity): CommonResult<String>
    fun loginByWeibo(weiboEntity: WeiboEntity): CommonResult<String>
    fun getFriendDynamic(biliBiliEntity: BiliBiliEntity): CommonResult<List<BiliBiliPojo>>
    fun isLiveOnline(id: String): Boolean
    fun liveSign(biliBiliEntity: BiliBiliEntity): String
}