package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.GroupService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@PrivateController
class SettingController(
    private val groupService: GroupService,
    @Value("\${yuq.art.master}") private val master: String
) {

    @Action("群开启 {groupNo}")
    @Synonym(["群关闭 {groupNo}"])
    fun openOrClose(groupNo: Long, @PathVar(0) op: String): String {
        val groupEntity = groupService.findByGroup(groupNo) ?: return "不存在该群"
        val s = op.contains("开启")
        groupEntity.config.switch = s.toStatus()
        groupService.save(groupEntity)
        return "群${groupNo}${if (s) "开启" else "关闭"}成功"
    }

    @Action("退群 {groupNo}")
    fun ss(groupNo: Long): String {
        val group = yuq.groups[groupNo]
        return if (group == null) "不存在该群"
        else {
            group.leave()
            "退出群${groupNo}成功"
        }
    }



}