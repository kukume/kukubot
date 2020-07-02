package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface SuperCuteService {
    fun getInfo(token: String): CommonResult<Map<String, String>>
    fun dailySign(map: Map<String, String>): String
    fun dailyVitality(map: Map<String, String>): String
    fun moreVitality(map: Map<String, String>): String
    fun feeding(map: Map<String, String>): String
    fun receiveCoin(map: Map<String, String>): Int
    fun finishTask(map: Map<String, String>): String
    fun steal(map: Map<String, String>): String
    fun findCute(map: Map<String, String>): String
    fun dailyLottery(map: Map<String, String>): String
    fun getProfile(map: Map<String, String>): String
}