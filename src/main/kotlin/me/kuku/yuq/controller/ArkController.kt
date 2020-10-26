package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import me.kuku.yuq.data.ArkNightsPool
import me.kuku.yuq.data.ArkPools
import me.kuku.yuq.entity.UserRecord
import me.kuku.yuq.service.ArkService
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.error.SkipMe
import javax.inject.Inject

@PrivateController
@GroupController
class ArkController {


    @Inject
    lateinit var ark: ArkService

    @Action("{pool}十连")
    @QMsg(at = true, atNewLine = true)
    fun cardTen(qq: Contact, pool: String): String {
        val p = ArkPools[pool] ?: throw SkipMe()
        return try {
            val l = qq(p)(10)
            val sb = StringBuilder( "您的十连抽卡结果为：")
            for (s in l) {
                sb.append("\n").append(s)
            }
            p.description?.let { sb.append("\n").append(it) }
            sb.toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            "系统异常！"
        }
    }

    @Action("{pool}单抽")
    @QMsg(at = true, atNewLine = true)
    fun cardOne(qq: Contact, pool: String): String {
        val p = ArkPools[pool] ?: throw SkipMe()
        return try {
            val l = qq(p)(1)
            val sb = StringBuilder("您的单抽抽卡结果为：")
            for (s in l) {
                sb.append("\n").append(s)
            }
            p.description?.let { sb.append("\n").append(it) }
            sb.toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            "系统异常！"
        }
    }

    operator fun Contact.invoke(pool: ArkNightsPool) = ark.getUserRecord(this.id, pool)

    operator fun UserRecord.invoke(num: Int) = ark.card(this, num)

}