package me.kuku.yuq.web

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Default
import com.IceCreamQAQ.YuWeb.annotation.WebController
import me.kuku.yuq.entity.ExceptionLogService
import me.kuku.yuq.pojo.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import javax.inject.Inject

@WebController
class ExceptionLogController @Inject constructor(
    private val exceptionLogService: ExceptionLogService
){

    @Action("/exceptionLog")
    fun find(@Default("1") pageNum: Int, @Default("20") size: Int): Any {
        return exceptionLogService.findAll(PageRequest.of(pageNum - 1, size, Sort.by("id")))
    }

}