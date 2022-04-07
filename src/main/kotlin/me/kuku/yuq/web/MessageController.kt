package me.kuku.yuq.web

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.YuWeb.annotation.WebController
import com.alibaba.fastjson.JSON
import me.kuku.yuq.entity.MessageService
import me.kuku.yuq.pojo.Page
import me.kuku.pojo.Result
import me.kuku.pojo.ResultStatus
import me.kuku.utils.JacksonUtils
import me.kuku.yuq.transaction
import javax.inject.Inject

@WebController
class MessageController @Inject constructor(
    private val messageService: MessageService
) {

//    @Action("/message/list")
//    suspend fun list(group: Long?, content: String?, qq: Long?, page: Page): Result<*> = transaction {
//        val p = messageService.findAll(group, content, qq, page.toPageRequest())
//        Result.success(JSON.parse(JacksonUtils.toJsonString(p)))
//    }

    @Action("/message/recall")
    fun recall(group: Long, id: Int): Result<*> {
        val messageEntity = messageService.findByMessageIdAndGroup(id, group)
            ?: return Result.failure(ResultStatus.DATA_NOT_EXISTS, null)
//        messageEntity.messageSource?.toArtGroupMessageSource()?.recall()
        return Result.success()
    }

}