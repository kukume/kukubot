package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.NeTeaseEntity
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface NeTeaseLogic {
    fun loginByPhone(username: String, password: String): CommonResult<NeTeaseEntity>
    fun loginByQQ(qqEntity: QQEntity): CommonResult<NeTeaseEntity>
    fun sign(neTeaseEntity: NeTeaseEntity): String
    fun listeningVolume(neTeaseEntity: NeTeaseEntity): String
}