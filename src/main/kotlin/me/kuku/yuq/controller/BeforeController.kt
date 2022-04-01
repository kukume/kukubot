package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.After
import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Global
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.BotActionContext
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.entity.Member
import com.icecreamqaq.yuq.entity.MessageAt
import com.icecreamqaq.yuq.message.Message
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.transaction
import javax.inject.Inject

@PrivateController
@GroupController
class BeforeController @Inject constructor(
    private val qqService: QqService
){

    @Before(weight = -1)
    @Global
    fun before(session: ContextSession, qq: Long, group: Long?) {
        transaction {
            val qqEntity = qqService.findByQq(qq)
            session["qqEntity"] = qqEntity!!
            if (group != null) {
                val groupEntity = qqEntity.groups.first { it.group == group }
                session["groupEntity"] = groupEntity
            }
        }
    }

    @After
    @Global
    fun after(message: Message, context: BotActionContext, qq: Long) {
        val source = context.source
        if (source is Member || source is Group) {
            if (message.at == null) {
                message.at = MessageAt(qq, true)
            }
        }
    }

}