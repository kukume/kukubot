package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.fasterxml.jackson.databind.JsonNode
import me.kuku.utils.OkHttpKtUtils
import me.kuku.yuq.entity.GroupService
import me.kuku.yuq.entity.QqService
import me.kuku.yuq.entity.Status
import me.kuku.yuq.utils.YuqUtils
import org.springframework.stereotype.Component

@JobCenter
@Component
class YaoHuoJob(
    private val qqService: QqService,
    private val groupService: GroupService
) {

    private var id = 0

    @Cron("2m")
    suspend fun post() {
        val jsonNode = OkHttpKtUtils.getJson("https://api.kukuqaq.com/yaohuo")
        if (jsonNode.size() == 0) return
        val newList = mutableListOf<JsonNode>()
        if (id != 0) {
            for (node in jsonNode) {
                if (node["id"].asInt() <= id) break
                newList.add(node)
            }
        }
        id = jsonNode[0]["id"].asInt()
        for (node in newList) {
            val qqList = qqService.findAll().filter { it.config.yaoHuoPush == Status.ON }
            for (qqEntity in qqList) {
                val str = """
                    妖火网有新帖了！！！
                    标题：${node["title"].asText()}
                    时间：${node["time"].asText()}
                    内容：${node["content"].asText()}
                    链接：${node["url"].asText()}
                """.trimIndent()
                YuqUtils.sendMessage(qqEntity, str)
            }
        }
    }


}