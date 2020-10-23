package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQLoginEntity
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface QQMailLogic {

    fun getFile(qqLoginEntity: QQLoginEntity): CommonResult<List<Map<String, String>>>

    fun fileRenew(qqLoginEntity: QQLoginEntity): String

}