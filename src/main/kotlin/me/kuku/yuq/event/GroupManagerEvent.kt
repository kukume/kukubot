@file:Suppress("unused")

package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.IceCreamQAQ.Yu.cache.EhcacheHelp
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.message.JsonEx
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.message.Text
import com.icecreamqaq.yuq.message.XmlEx
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.logic.QQAILogic
import me.kuku.yuq.service.QQService
import me.kuku.yuq.service.GroupService
import me.kuku.yuq.utils.BotUtils
import javax.inject.Inject
import javax.inject.Named

@EventListener
class GroupManagerEvent {
    @Inject
    private lateinit var groupService: GroupService
    @Inject
    private lateinit var qqService: QQService
    @Inject
    private lateinit var qqAiLogic: QQAILogic
    @Inject
    @field:Named("CommandCountOnTime")
    private lateinit var eh: EhcacheHelp<Int>

    @Event
    fun inter(e: GroupMessageEvent){
        val groupEntity = groupService.findByGroup(e.group.id) ?: return
        if (e.sender.id.toString() in groupEntity.whiteJsonArray) return
        val message = e.message
        val str = try {
            message.firstString()
        }catch (e: IllegalStateException){
            null
        }
        if (str != null) {
            groupEntity.interceptJsonArray.forEach {
                val intercept = it as String
                if (intercept in str) {
                    e.cancel = true
                }
            }
        }
        if (yuq.groups[e.group.id]?.bot?.isAdmin() != true) return
        val qqEntity = qqService.findByQQAndGroup(e.sender.id, e.group.id)
                ?: QQEntity(null, e.sender.id, groupEntity, 0)
        val violationJsonArray = groupEntity.violationJsonArray
        var code = 0
        var vio: String? = null
        out@for (i in violationJsonArray.indices){
            val violation = violationJsonArray.getString(i)
            for (item in message.body){
                when (item){
                    is Text -> if (item.text.contains(violation)) code = 1
                    is Image -> {
                        val result = qqAiLogic.generalOCR(item.url)
                        if (violation in result) code = 1
                        val b = qqAiLogic.pornIdentification(item.url)
                        if (b) code = 2
                    }
                    is XmlEx -> if (violation in item.value) code = 1
                    is JsonEx -> if (violation in item.value) code = 1
                }
                if (code != 0) {
                    vio = violation
                    break@out
                }
            }
        }
        if (code != 0) {
            qqEntity.violationCount = qqEntity.violationCount + 1
            if (qqEntity.violationCount < groupEntity.maxViolationCount) {
                val sb = StringBuilder()
                if (code == 2) sb.append("检测到色情图片。")
                else sb.append("检测到违规词\"$vio\"。")
                sb.append("您当前的违规次数为${qqEntity.violationCount}次，累计违规${groupEntity.maxViolationCount}次会被移出本群哦！！")
                e.group.sendMessage(mif.at(qqEntity.qq).plus(sb.toString()))
                e.message.recall()
                e.sender.ban(60 * 30)
                qqService.save(qqEntity)
            }else{
                e.sender.kick("违规次数已达上限")
                e.group.sendMessage("${qqEntity.qq}违规次数已达上限，送飞机票一张！！".toMessage())
            }
        }
    }

    @Event
    fun qa(e: GroupMessageEvent){
        val groupEntity = groupService.findByGroup(e.group.id) ?: return
        val message = e.message
        if (message.toPath().isEmpty()) return
        if (message.toPath()[0] == "删问答") return
        val str = try {
            message.firstString()
        }catch (e: IllegalStateException){
            return
        }
        val qaJsonArray = groupEntity.qaJsonArray
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            val type = jsonObject.getString("type")
            val q = jsonObject.getString("q")
            var status = false
            if (type == "ALL"){
                if (q == str) status = true
            }else {
                if (jsonObject.getString("q") in str) status = true
            }
            if (status){
                val maxCount = groupEntity.maxCommandCountOnTime
                val key = e.sender.toString() + q
                var num = eh[key] ?: 0
                if (num >= maxCount) return
                eh[key] = ++num
                val jsonArray = jsonObject.getJSONArray("a")
                e.group.sendMessage(BotUtils.jsonArrayToMessage(jsonArray))
            }
        }
    }
}
