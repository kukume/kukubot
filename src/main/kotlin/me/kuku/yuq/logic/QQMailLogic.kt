package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface QQMailLogic {

    fun getFile(qqEntity: QQEntity): CommonResult<List<Map<String, String>>>

    fun fileRenew(qqEntity: QQEntity): String

}