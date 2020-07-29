package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind

@AutoBind
interface PiXivLogic {
    fun getImage(url: String): ByteArray

    fun searchTag(tag: String): String

    fun bookMarks(id: String, cookie: String): String
}