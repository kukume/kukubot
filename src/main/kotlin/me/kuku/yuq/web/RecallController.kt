package me.kuku.yuq.web

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.YuWeb.annotation.WebController
import me.kuku.yuq.entity.RecallService
import me.kuku.yuq.pojo.Page
import me.kuku.pojo.Result
import org.springframework.transaction.support.TransactionTemplate
import javax.inject.Inject

@WebController
class RecallController @Inject constructor(
    private val recallService: RecallService,
    private val transactionTemplate: TransactionTemplate
) {

    @Action("/recall/list")
    fun recallList(group: Long?, messageId: Int?, content: String?, qq: Long?, page: Page): Result<*>? = transactionTemplate.execute {
        Result.success(recallService.findByAll(group, messageId, content, qq, page.toPageRequest()))
    }

}