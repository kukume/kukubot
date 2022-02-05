package me.kuku.yuq.pojo

import org.springframework.data.domain.PageRequest

data class Page(var pageNum: Int = 1, var pageSize: Int = 20) {
    fun toPageRequest(): PageRequest = PageRequest.of(pageNum - 1, pageSize)
}