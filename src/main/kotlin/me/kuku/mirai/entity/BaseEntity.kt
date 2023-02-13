package me.kuku.mirai.entity

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

open class BaseEntity {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @CreatedDate
    var createTime: LocalDateTime = LocalDateTime.now()
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @LastModifiedDate
    var updateTime: LocalDateTime = LocalDateTime.now()
}

enum class Status {
    ON, OFF
}

fun String.toStatus(): Status {
    return if (this == "开") Status.ON
    else Status.OFF
}
