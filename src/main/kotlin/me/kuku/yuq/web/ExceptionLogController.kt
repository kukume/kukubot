package me.kuku.yuq.web

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.kuku.ktor.plugins.pageable
import me.kuku.yuq.entity.ExceptionLogService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class ExceptionLogController(
    private val exceptionLogService: ExceptionLogService
) {

    fun Routing.exceptionLog() {

        route("exceptionLog") {
            get {
                val pageable = call.request.queryParameters.pageable() as PageRequest
                val page = exceptionLogService.findAll(pageable.withSort(Sort.by(Sort.Direction.DESC, "id")))
                for (exceptionLogEntity in page) {
                    exceptionLogEntity.messageEntity?.qqEntity = null
                    exceptionLogEntity.messageEntity?.groupEntity = null
                    exceptionLogEntity.privateMessageEntity?.qqEntity = null
                }
                call.respond(page)
            }
        }
    }


}