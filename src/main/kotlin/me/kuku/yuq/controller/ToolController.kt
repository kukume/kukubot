package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.toCodeString
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.message.Message.Companion.toMessageByRainCode
import com.icecreamqaq.yuq.mif
import me.kuku.pojo.QqLoginPojo
import me.kuku.utils.MyUtils
import me.kuku.utils.OkHttpUtils
import me.kuku.yuq.entity.GroupEntity
import me.kuku.yuq.entity.MessageService
import me.kuku.yuq.entity.QaType
import me.kuku.yuq.logic.ToolLogic
import me.kuku.yuq.utils.YuqUtils
import javax.inject.Inject

@GroupController
class ToolController @Inject constructor(
    private val messageService: MessageService
) {

    @Action("摸鱼日历")
    fun fishermanCalendar(group: Group) {
        val bytes = OkHttpUtils.getBytes("https://api.kukuqaq.com/tool/fishermanCalendar?preview")
        group.sendMessage(mif.imageByByteArray(bytes))
    }

    @Action("摸鱼日历搜狗")
    fun fishermanCalendarSoGou(group: Group) {
        val bytes = OkHttpUtils.getBytes("https://api.kukuqaq.com/tool/fishermanCalendar/sogou?preview")
        group.sendMessage(mif.imageByByteArray(bytes))
    }

    @Action("色图")
    fun color() =
        mif.imageByUrl(OkHttpUtils.get("https://api.kukuqaq.com/lolicon/random?preview").also { it.close() }.header("location")!!)

    @Action(value = "读消息", suffix = true)
    @QMsg(reply = true)
    fun readMessage(message: Message, group: Long): String? {
        val messageSource = message.reply ?: return null
        val id = messageSource.id
        val messageEntity = messageService.findByMessageIdAndGroup(id, group) ?: return "没有找到该消息"
        return messageEntity.content
    }

    @Action("\\.*\\")
    fun qa(group: Group, groupEntity: GroupEntity, message: Message) {
        message.recall()
        val codeStr = message.toCodeString()
        val qaList = groupEntity.config.qaList
        for (qa in qaList) {
            if (qa.q == codeStr && qa.type == QaType.EXACT) {
                group.sendMessage(qa.a.toMessageByRainCode())
            }
            if (codeStr.contains(qa.q) && qa.type == QaType.FUZZY) {
                group.sendMessage(qa.a.toMessageByRainCode())
            }
        }
    }

    @Action("百科 {text}")
    @QMsg(reply = true)
    fun baiKe(text: String) = ToolLogic.baiKe(text)

    @Action("龙王")
    fun dragonKing(group: Long, qq: Long, @PathVar(value = 1, type = PathVar.Type.Integer) num: Int?): Any {
        val groupQqLoginPojo = YuqUtils.groupQqLoginPojo()
        val list = QqGroupLogic.groupHonor(groupQqLoginPojo, group, GroupHonorType.TALK_AT_IVE)
        if (list.isEmpty()) return mif.at(qq).plus("没有发现龙王")
        val newNum = num ?: 1
        if (newNum > list.size) return mif.at(qq).plus("历史龙王只有${list.size}位哦，超过范围了")
        val groupHonor = list[newNum - 1]
        if (groupQqLoginPojo.qq == groupHonor.qq) return listOf("呼风唤雨", "84消毒", "巨星排面").random().toMessage()
        val urlList = listOf(
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/0bd0d4c6-0ebb-4811-ba06-a0d65c3a8ed3.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/32c1791e-0cb5-4888-a99f-dd8bdd654423.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/493dfe13-bebb-4cd7-8d77-d0bde395db68.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c4cfb6b0-1e67-4f23-9d6e-80a03fb5f91f.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/568877f3-f62b-4cc1-97ee-0d48da8dfb59.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/f39ebff2-03c0-4cee-8967-206562cc055e.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8bbf31d5-878b-4d42-9aa0-a41fd8e13ea6.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c3aa3d94-5cf7-47e1-ba56-db9116b1bcae.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/d13d84e5-e7fa-4d1b-ae6c-1413ffc78769.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/b465c944-8373-4d8c-beda-56eb7c24fa0b.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/b049eb61-dca3-4541-b3dd-c220ccd94595.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/682c4454-fc52-41c3-9c44-890aaa08c03d.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/00d716cf-f691-42ea-aa71-e28f18a3b4b3.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8635cd24-5d87-4fc8-b429-425e02b22849.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/7309fe37-7e34-4b7e-9304-5a1a854d251c.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c631afd3-9614-403c-a5a1-18413bbe3374.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/cfa9129d-e99d-491b-932d-e353ce7ca2d8.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/40960b38-781d-43b0-863b-8962a5342020.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/c3e83c57-242a-4843-af51-85a84f7badaf.gif",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8e4d291b-e6ba-48d9-b8f9-3adc56291c27.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/8bcad94b-aff5-4e81-af89-8a1007eda4ae.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/dc8403a0-caec-40e0-98a8-93abdb263712.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/1468ee00-a106-42c7-9ce3-0ced6b2ddc3e.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/959cd1ef-8731-4379-b1ad-0d3bf66e38c0.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/79c484e0-695c-49e9-9514-bcbe294ca7c6.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/f9b48126-fb7e-4482-b5ce-140294f57066.png",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/732a6387-2595-4c56-80f8-c52fce6214bb.jpg",
            "https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/bb768136-d96d-451d-891a-5f409f7fbff1.jpg"
        )
        return mif.at(groupHonor.qq).plus(mif.imageByUrl(urlList.random())).plus("龙王，已上位${groupHonor.desc}，快喷水")
    }

}

object QqGroupLogic {

    fun groupHonor(qqLoginPojo: QqLoginPojo, group: Long, type: GroupHonorType): List<GroupHonor> {
        val typeNum: Int
        val wwv: Int
        val param: String
        val image: String
        val list = mutableListOf<GroupHonor>()
        when (type) {
            GroupHonorType.TALK_AT_IVE -> {
                typeNum = 1
                wwv = 129
                param = "talkativeList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-drgon.png"
            }
            GroupHonorType.LEGEND -> {
                typeNum = 3
                wwv = 128
                param = "legendList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-fire-big.png"
            }
            GroupHonorType.ACTOR -> {
                typeNum = 2
                wwv = 128
                param = "actorList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-fire-small.png"
            }
            GroupHonorType.STRONG_NEW_BIE -> {
                typeNum = 5
                wwv = 128
                param = "strongnewbieList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-shoots-small.png"
            }
            GroupHonorType.EMOTION -> {
                typeNum = 6
                wwv = 128
                param = "emotionList"
                image = "https://qq-web.cdn-go.cn/qun.qq.com_interactive/067dafcc/app/qunhonor/dist/cdn/assets/images/icon-happy-stream.png"
            }
        }
        val html = OkHttpUtils.getStr("https://qun.qq.com/interactive/honorlist?gc=$group&type=$typeNum&_wv=3&_wwv=$wwv",
            OkHttpUtils.addCookie(qqLoginPojo.cookieWithPs))
        val jsonStr = MyUtils.regex("window.__INITIAL_STATE__=", "</script", html)
        val jsonObject = JSON.parseObject(jsonStr)
        val jsonArray = jsonObject.getJSONArray(param)
        jsonArray.forEach {
            val singleJsonObject = it as JSONObject
            list.add(GroupHonor(singleJsonObject.getLong("uin"),
                singleJsonObject.getString("name"), singleJsonObject.getString("desc"),
                image))
        }
        return list
    }
}

enum class GroupHonorType {
    TALK_AT_IVE,
    LEGEND,
    ACTOR,
    STRONG_NEW_BIE,
    EMOTION
}

data class GroupHonor(
    var qq: Long,
    var name: String,
    var desc: String,
    var image: String
)