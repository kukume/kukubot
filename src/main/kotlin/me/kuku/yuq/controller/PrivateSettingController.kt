package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import me.kuku.yuq.entity.QQGroupEntity
import me.kuku.yuq.logic.QQGroupLogic
import me.kuku.yuq.service.QQGroupService
import javax.inject.Inject

@PrivateController
class PrivateSettingController {
    @Inject
    private lateinit var qqGroupLogic: QQGroupLogic
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String

    @Action("群开启 {groupNo}")
    @Synonym(["群关闭 {groupNo}"])
    fun groupOpen(groupNo: Long, @PathVar(0) str: String, qq: Long): String{
        if (qq != master.toLong()) return "您不是机器人主人，无法执行！！"
        val commonResult = qqGroupLogic.queryGroup()
        val list = commonResult.t ?: return commonResult.msg
        return if (groupNo in list){
            val qqGroupEntity = qqGroupService.findByGroup(groupNo) ?: QQGroupEntity(null, groupNo)
            qqGroupEntity.status = str.contains("开启")
            qqGroupService.save(qqGroupEntity)
            "机器人开启或者关闭成功！！"
        }else "机器人并没有加入这个群！！"
    }
}