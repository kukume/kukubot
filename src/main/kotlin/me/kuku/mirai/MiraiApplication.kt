package me.kuku.mirai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MiraiApplication

fun main(args: Array<String>) {
    runApplication<MiraiApplication>(*args)
}
