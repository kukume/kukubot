package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import me.kuku.yuq.service.WeiboService
import javax.inject.Inject

@GroupController
class WeiboController {
    @Inject
    lateinit var weiboService: WeiboService

    @Action("热搜")
    fun hotSearch() = weiboService.hotSearch()

}