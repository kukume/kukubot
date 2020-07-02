package me.kuku.yuq.service

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.*

@AutoBind
interface DaoService {
    fun saveOrUpdateQQ(entity: QQEntity)
    fun saveOrUpdateMotion(entity: MotionEntity)
    fun saveOrUpdateSuperCute(entity: SuperCuteEntity)
    fun saveOrUpdateSteam(entity: SteamEntity)
    fun saveOrUpdateQQJob(entity: QQJobEntity)

    fun delQQ(qqEntity: QQEntity)
    fun findQQByQQ(qq: Long): QQEntity?
    fun findQQJobByQQAndType(qq: Long, type: String): QQJobEntity?
    fun findMotionByQQ(qq: Long): MotionEntity?
    fun findSuperCuteByQQ(qq: Long): SuperCuteEntity?
    fun findSteamByQQ(qq: Long): SteamEntity?
    fun findQQJobByQQ(qq: Long): MutableList<Any?>?

    fun findQQByActivity(): MutableList<Any?>?
    fun findQQByAll(): MutableList<Any?>?
    fun findMotionByAll(): MutableList<Any?>?
    fun findQQJobByType(type: String): MutableList<Any?>?
}