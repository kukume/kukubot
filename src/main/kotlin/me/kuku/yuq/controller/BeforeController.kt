package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Before
import com.IceCreamQAQ.Yu.annotation.Global
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.controller.ContextSession
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.transaction
import javax.inject.Inject

@PrivateController
@GroupController
class BeforeController @Inject constructor(
    private val qqService: QqService
){

    @Before
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

}