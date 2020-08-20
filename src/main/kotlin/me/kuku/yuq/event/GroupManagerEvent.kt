package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Config
import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.alibaba.fastjson.JSONArray
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.toMessage
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.GroupQQEntity
import me.kuku.yuq.logic.QQAILogic
import me.kuku.yuq.service.GroupQQService
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject

@EventListener
class GroupManagerEvent {
    @Inject
    private lateinit var qqGroupService: QQGroupService
    @Inject
    private lateinit var groupQQService: GroupQQService
    @Inject
    private lateinit var qqAiLogic: QQAILogic
    @Config("YuQ.Mirai.bot.master")
    private lateinit var master: String

    @Event(weight = Event.Weight.high)
    fun switchGroup(e: GroupMessageEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id)
        val body = e.message.body
        if (body.size < 1) return
        val msg = body[0].toPath()
        if (!msg.startsWith("机器人")) {
            if (qqGroupEntity?.status != true) {
                e.cancel = true
            }
        }
    }

    @Event(weight = Event.Weight.low)
    fun intercept(e: GroupMessageEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: return
        val qq = e.sender.id
        val whiteJsonArray = qqGroupEntity.getWhiteJsonArray()
        if (whiteJsonArray.contains(qq) || qq == master.toLong()) return
        val msg = e.message.toPath()[0]
        val interceptJsonArray = qqGroupEntity.getInterceptJsonArray()
        if (interceptJsonArray.contains(msg)) e.cancel = true
    }

    @Event
    fun keyword(e: GroupMessageEvent){
        val group = e.group.id
        if (yuq.groups[group]?.bot?.isAdmin() != true) return
        val qq = e.sender.id
        if (yuq.groups[group]?.get(qq)?.isAdmin() == true) return
        val qqGroupEntity = qqGroupService.findByGroup(group) ?: return
        val keywordJsonArray = qqGroupEntity.getKeywordJsonArray()
        for (i in keywordJsonArray.indices){
            val keyword = keywordJsonArray.getString(i)
            if (keyword in e.message.sourceMessage.toString()){
                e.message.recall()
                val maxCount = qqGroupEntity.maxViolationCount ?: 5
                val violation = this.violation(qq, group, maxCount, e)
                if (violation >= maxCount) return
                e.sender.ban(10 * 60)
                e.group.sendMessage(mif.at(qq).plus(
                        "检测到违规词\"$keyword\"，您已被禁言。\n您当前的违规次数为${violation}次。\n累计违规${maxCount}次会被踢出本群哦！！"))
                e.cancel = true
            }
        }
    }

    @Event
    fun qa(e: GroupMessageEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.group.id) ?: return
        val message = e.message
        if (message.toPath().isEmpty()) return
        if (message.toPath()[0] == "删问答") return
        val qaJsonArray = qqGroupEntity.getQaJsonArray()
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            if (jsonObject.getString("q") in message.body[0].toPath()){
                val textAnswer = jsonObject.get("a")
                if (textAnswer is String) {
                    if (textAnswer.startsWith("<?xml version=")) {
                        e.group.sendMessage(mif.xmlEx(BotUtils.regex("serviceID=\"", "\"", textAnswer)!!.toInt(), textAnswer).toMessage())
                    } else e.group.sendMessage(textAnswer.toMessage())
                } else {
                    val aJsonArray = textAnswer as JSONArray
                    val msg = BotUtils.jsonArrayToMessage(aJsonArray)
                    e.group.sendMessage(msg)
                }
                return
            }
        }
    }

    @Event
    fun pic(e: GroupMessageEvent){
        val group = e.group.id
        val qq = e.sender.id
        if (yuq.groups[group]?.bot?.isAdmin() != true) return
        val qqGroupEntity = qqGroupService.findByGroup(group) ?: return
        if (qqGroupEntity.pic == true){
            val bodyList = e.message.body
            for (body in bodyList){
                if (body is Image){
                    val url = body.url
                    val b = qqAiLogic.pornIdentification(url)
                    if (b){
                        e.message.recall()
                        val maxCount = qqGroupEntity.maxViolationCount ?: 5
                        val violation = this.violation(qq, group, maxCount, e)
                        if (violation >= 5) return
                        e.sender.ban(10 * 60)
                        e.group.sendMessage(mif.at(qq).plus(
                                "检测到色情图片，您已被禁言\n您当前的违规次数为${violation}次。\n累计违规${maxCount}次会被踢出本群哦！！"))
                    }
                }
            }
        }
    }

    private fun violation(qq: Long, group: Long, count: Int, e: GroupMessageEvent): Int{
        val groupQQEntity = groupQQService.findByQQAndGroup(qq, group) ?: GroupQQEntity(null, qq, group)
        val nextViolationCount = groupQQEntity.violationCount + 1
        if (nextViolationCount >= count) {
            e.sender.kick()
            e.group.sendMessage("${qq}违禁次数已达上限，被移走了！！".toMessage())
        }else {
            groupQQEntity.violationCount = nextViolationCount
            groupQQService.save(groupQQEntity)
        }
        return nextViolationCount
    }

}
