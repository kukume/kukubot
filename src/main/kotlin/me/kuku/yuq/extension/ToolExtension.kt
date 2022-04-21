package me.kuku.yuq.extension

import me.kuku.utils.OkHttpKtUtils
import me.kuku.yuq.utils.abilityDefault
import me.kuku.yuq.utils.send
import org.telegram.abilitybots.api.util.AbilityExtension

class ToolExtension: AbilityExtension {


    fun ss() = abilityDefault("oracle", "查询oracle信息", 1) {
        val email = arguments()[0]
        val ss = if (OkHttpKtUtils.getJson("https://api.kukuqaq.com/tool/oracle/promotion?email=$email").getJSONArray("items")?.isNotEmpty() == true) "有资格了"
        else "你木的资格"
        send("$email：$ss")
    }


}