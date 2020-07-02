package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind

@AutoBind
interface WeiboService {
    fun hotSearch(): String
}