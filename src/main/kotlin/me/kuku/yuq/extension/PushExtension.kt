package me.kuku.yuq.extension

import me.kuku.yuq.entity.TgPushEntity
import me.kuku.yuq.entity.TgPushService
import me.kuku.yuq.utils.abilityDefault
import me.kuku.yuq.utils.silent
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.util.AbilityExtension
import java.util.*
import javax.inject.Inject

class PushExtension @Inject constructor(
    private val tgPushService: TgPushService
): AbilityExtension {

    private fun key(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }

    fun newKey() = abilityDefault("new", "create a key") {
        val id = user().id
        val tgPushEntity = tgPushService.findByUserid(id)
        if (tgPushEntity != null) {
            silent().sendMd("You have already generated the key, if you need to regenerate it, you can use the /regen command",
                chatId())
        } else {
            val key = key()
            tgPushService.save(TgPushEntity().also { entity ->
                entity.key = key
                entity.userid = id
            })
            silent().sendMd("""
                    Success!
                    You key is `${key}`
                """.trimIndent(), chatId())
        }
    }

    fun regen(): Ability = Ability
        .builder()
        .name("regen")
        .info("regen a key")
        .input(0)
        .locality(Locality.ALL)
        .privacy(Privacy.PUBLIC)
        .action {
            val id = it.user().id
            val userEntity = tgPushService.findByUserid(id)
            val silent = it.bot().silent()
            if (userEntity == null) {
                silent.sendMd("You have not generated a key, if you want to generate a key, please use the /new command",
                    it.chatId())
            }else {
                val newKey = key()
                userEntity.key = newKey
                tgPushService.save(userEntity)
                silent.sendMd("""
                    Success!
                    You new key is `${newKey}`
                """.trimIndent(), it.chatId())
            }
        }
        .build()

    fun query(): Ability = Ability
        .builder()
        .name("query")
        .info("query my key")
        .input(0)
        .locality(Locality.ALL)
        .privacy(Privacy.PUBLIC)
        .action {
            val id = it.user().id
            val userEntity = tgPushService.findByUserid(id)
            val silent = it.bot().silent()
            if (userEntity == null) {
                silent.sendMd("You have not generated a key, if you want to generate a key, please use the /new command",
                    it.chatId())
            }else {
                val key = userEntity.key
                silent.sendMd("""
                    Success!
                    You key is `${key}`
                """.trimIndent(), it.chatId())
            }
        }
        .build()

}