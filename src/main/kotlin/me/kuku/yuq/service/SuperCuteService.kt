package me.kuku.yuq.service

import me.kuku.yuq.pojo.CommonResult

interface SuperCuteService {
    fun getInfo(token: String): CommonResult<Map<String, String>>
    fun dailySign(map: Map<String, String>): String
    fun dailyVitality(map: Map<String, String>): String
    fun moreVitality(map: Map<String, String>): String
    fun feeding(map: Map<String, String>): String
    fun finishTask(map: Map<String, String>): String
    fun steal(map: Map<String, String>): String
    fun findCute(map: Map<String, String>): String
    fun dailyLottery(map: Map<String, String>): String
    fun getProfile(map: Map<String, String>): String
}