package me.kuku.yuq.utils

import kotlinx.coroutines.runBlocking
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.MessageContext
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.abilitybots.api.sender.SilentSender


fun abilityDefault(): Ability.AbilityBuilder = Ability.builder().locality(Locality.ALL).privacy(Privacy.PUBLIC)


fun abilityDefault(name: String, info: String, input: Int = 0, block: suspend MessageContext.() -> Unit): Ability {
    return Ability.builder().locality(Locality.ALL).privacy(Privacy.PUBLIC).name(name).info(info).input(input).action {
        runBlocking {
            block.invoke(it)
        }
    }.build()
}

fun MessageContext.silent(): SilentSender = this.bot().silent()