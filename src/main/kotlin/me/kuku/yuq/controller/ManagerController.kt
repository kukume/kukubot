package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Config
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.mif
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.Status
import javax.inject.Inject

@GroupController
class ManagerController @Inject constructor(
    private val groupService: GroupService,
    @Config("YuQ.ArtQQ.master") private val master: String
){
    @Before(except = ["operateStatus"])
    fun before(qq: Long) {
        if (qq != master.toLong()) throw mif.at(qq).plus("权限不足，无法执行").toThrowable()
    }

    @Action("{operate} {status}")
    fun operateStatus(operate: String, status: Boolean, groupEntity: GroupEntity, qq: Long): String? {
        when (operate) {
            "loc群推送" -> groupEntity.config.locPush = status.toStatus()
            "复读" -> groupEntity.config.repeat = status.toStatus()
            else -> return null
        }
        before(qq)
        groupService.save(groupEntity)
        return "${operate}${if (status) "开启" else "关闭"}成功"
    }

}

fun Boolean.toStatus() = if (this) Status.ON else Status.OFF
