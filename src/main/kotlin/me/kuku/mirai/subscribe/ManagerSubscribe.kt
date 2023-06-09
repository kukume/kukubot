package me.kuku.mirai.subscribe

import me.kuku.mirai.config.MiraiConfig
import me.kuku.mirai.entity.*
import me.kuku.mirai.utils.GroupMessageSubscribe
import me.kuku.mirai.utils.at
import me.kuku.mirai.utils.firstArg
import me.kuku.mirai.utils.secondArg
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.nextMessage
import org.springframework.stereotype.Component

@Component
class ManagerSubscribe(
    private val groupService: GroupService,
    private val miraiConfig: MiraiConfig,
    private val essenceService: EssenceService
) {

    suspend fun GroupMessageSubscribe.manager() {
        before {
            val groupEntity = groupService.findByGroup(group.id) ?: GroupEntity().also {
                it.group = group.id
            }
            groupService.save(groupEntity)
            val qq = sender.id
            if (qq != miraiConfig.master) error("权限不足")
            set(groupEntity)
        }
        "问" atReply {
            subject.sendMessage(at() + "请发送问题")
            val q = nextMessage(30000).serializeToMiraiCode()
            subject.sendMessage(at() + "请发送回复的内容")
            val a = nextMessage(30000).serializeToMiraiCode()
            subject.sendMessage(at() + """
                请发送问答匹配的类型
                1、消息和问题一致
                2、消息包含问题
                3、消息以问题开始
                4、消息以问题结尾
            """.trimIndent())
            val numMessage = nextMessage(30000) { it.message.contentToString().toIntOrNull() in listOf(1, 2, 3, 4) }
            val num = numMessage.contentToString().toInt()
            val qa = Qa()
            qa.q = q
            qa.a = a
            qa.type = when(num) {
                1 -> Qa.Type.Eq
                2 -> Qa.Type.Like
                3 -> Qa.Type.StartsWith
                4 -> Qa.Type.EndsWith
                else -> Qa.Type.Eq
            }
            val groupEntity = firstAttr<GroupEntity>()
            groupEntity.qa.add(qa)
            groupService.save(groupEntity)
            "添加问答成功"
        }
        regex("消息转发 [-]?[0-9]+[ ]?[0-9]*") atReply {
            val chatId = firstArg<PlainText>().content.toLong()
            val messageThreadId = try {
                secondArg<PlainText>().content.toInt()
            } catch (e: Exception) {
                null
            }
            val groupEntity = firstAttr<GroupEntity>()
            groupEntity.forward.chatId = chatId
            groupEntity.forward.messageThreadId = messageThreadId
            groupService.save(groupEntity)
            "添加消息转发成功"
        }
        regex("精华消息转发 [-]?[0-9]+[ ]?[0-9]*") atReply {
            val chatId = firstArg<PlainText>().content.toLong()
            val messageThreadId = try {
                secondArg<PlainText>().content.toInt()
            } catch (e: Exception) {
                null
            }
            val essenceEntity = EssenceEntity()
            essenceEntity.chatId = chatId
            essenceEntity.messageThreadId = messageThreadId
            essenceEntity.group = group.id
            essenceService.save(essenceEntity)
            "添加消息转发成功"
        }
    }

}
