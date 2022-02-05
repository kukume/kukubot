package me.kuku.yuq.web

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.YuWeb.annotation.WebController
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.yuq
import me.kuku.pojo.Result
import me.kuku.pojo.ResultStatus
import me.kuku.utils.toJSONString
import me.kuku.yuq.controller.toStatus
import me.kuku.yuq.entity.GroupConfig
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.pojo.Page
import me.kuku.yuq.transaction
import javax.inject.Inject

@WebController
class GroupWebController @Inject constructor(
    private val groupService: GroupService
) {

    @Action("/group/list")
    fun groupList(group: Long?, page: Page = Page()): Result<*> = transaction {
        val groupPage = groupService.findAll(group, page.toPageRequest())
        val jsonObject = JSON.parseObject(groupPage.toJSONString())
        val jsonArray = jsonObject.getJSONArray("content")
        for (any in jsonArray) {
            val singleJsonObject = any as JSONObject
            val g = singleJsonObject.getLong("group")
            val groupObj = yuq.groups[g]
            singleJsonObject["name"] = groupObj?.name ?: ""
            singleJsonObject["avatar"] = groupObj?.avatar
        }
        Result.success(jsonObject)
    }

    @Action("/group/detail")
    fun groupDetail(id: Int): Result<*> {
        val groupEntity = groupService.findById(id)
            ?: return Result.failure("id不存在", null)
        val jsonObject = JSON.parseObject(groupEntity.toJSONString())
        val g = jsonObject.getLong("group")
        val groupObj = yuq.groups[g]
        jsonObject["name"] = groupObj?.name ?: ""
        jsonObject["avatar"] = groupObj?.avatar
        return Result.success(jsonObject)
    }

    @Action("/group/save")
    fun groupSave(id: Int, flashImageNotify: Boolean, leaveToBlack: Boolean, locPush: Boolean,
                  recallNotify: Boolean, repeat: Boolean): Result<*> {
        val groupEntity = groupService.findById(id)
            ?: return ResultStatus.DATA_NOT_EXISTS.toResult()
        groupEntity.config = GroupConfig().also {
            it.flashImageNotify = flashImageNotify.toStatus()
            it.leaveToBlack = leaveToBlack.toStatus()
            it.locPush = locPush.toStatus()
            it.recallNotify = recallNotify.toStatus()
            it.repeat = repeat.toStatus()
        }
        groupService.save(groupEntity)
        return Result.success()
    }

}