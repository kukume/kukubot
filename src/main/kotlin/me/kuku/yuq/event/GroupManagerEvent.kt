package me.kuku.yuq.event

import com.IceCreamQAQ.Yu.annotation.Event
import com.IceCreamQAQ.Yu.annotation.EventListener
import com.icecreamqaq.yuq.event.GroupMessageEvent
import com.icecreamqaq.yuq.firstString
import com.icecreamqaq.yuq.message.Image
import com.icecreamqaq.yuq.mf
import com.icecreamqaq.yuq.mif
import com.icecreamqaq.yuq.yuq
import me.kuku.yuq.service.QQGroupService
import me.kuku.yuq.utils.QQAIUtils
import javax.inject.Inject

@EventListener
class GroupManagerEvent {
    @Inject
    private lateinit var qqGroupService: QQGroupService

    @Event(weight = Event.Weight.high)
    fun switchGroup(e: GroupMessageEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.message.group!!)
        val msg = e.message.body[0].toPath()
        if (!msg.startsWith("机器人")) {
            if (qqGroupEntity?.status != true) {
                e.cancel = true
            }
        }
    }

    @Event
    fun keyword(e: GroupMessageEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.message.group!!) ?: return
        val keywords = qqGroupEntity.keyword.split("|")
        for (k in keywords){
            if (k == "") continue
            if (k in e.message.sourceMessage.toString()){
                e.message.recall()
                yuq.groups[e.message.group!!]?.members?.get(e.message.qq)?.ban(10 * 60)
                yuq.sendMessage(mf.newGroup(e.message.group!!).plus(mif.at(e.message.qq!!)).plus("检测到违规词\"$k\"，您已被禁言，您当前在本群违规次数："))
                return
            }
        }
    }

    @Event
    fun qa(e: GroupMessageEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.message.group!!) ?: return
        val qaJsonArray = qqGroupEntity.getQaJsonArray()
        for (i in qaJsonArray.indices){
            val jsonObject = qaJsonArray.getJSONObject(i)
            if (jsonObject.getString("q") in e.message.firstString()){
                yuq.sendMessage(mf.newGroup(e.message.group!!).plus(mif.at(e.message.qq!!)).plus(jsonObject.getString("a")))
                return
            }
        }
    }

    @Event
    fun pic(e: GroupMessageEvent){
        val qqGroupEntity = qqGroupService.findByGroup(e.message.group!!) ?: return
        if (qqGroupEntity.pic == true){
            val bodyList = e.message.body
            for (body in bodyList){
                if (body is Image){
                    val url = body.url
                    val b = QQAIUtils.pornIdentification(url)
                    if (b){
                        e.message.recall()
                        yuq.groups[e.message.group!!]?.members?.get(e.message.qq)?.ban(10 * 60)
                        yuq.sendMessage(mf.newGroup(e.message.group!!).plus(mif.at(e.message.qq!!)).plus("检测到色情图片，您已被禁言，您当前在本群违规次数："))
                    }
                }
            }
        }
    }

}