package me.kuku.yuq.service

import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult

interface QQMailService {

    fun getFile(qqEntity: QQEntity): CommonResult<List<Map<String, String>>>

    fun fileRenew(qqEntity: QQEntity): String

}