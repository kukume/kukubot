package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface QQMailService {

    fun getFile(qqEntity: QQEntity): CommonResult<List<Map<String, String>>>

    fun fileRenew(qqEntity: QQEntity): String

}