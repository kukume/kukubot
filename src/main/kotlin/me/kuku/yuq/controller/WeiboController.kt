package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import me.kuku.yuq.logic.WeiboLogic
import javax.inject.Inject

@GroupController
class WeiboController {
    @Inject
    lateinit var weiboLogic: WeiboLogic

    @Action("热搜")
    fun hotSearch() = weiboLogic.hotSearch()

}