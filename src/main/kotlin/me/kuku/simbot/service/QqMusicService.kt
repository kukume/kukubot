package me.kuku.simbot.service

import me.kuku.simbot.entity.QqEntity
import me.kuku.simbot.entity.QqMusicEntity
import me.kuku.simbot.entity.QqMusicRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource

interface QqMusicService {
    fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity?
    fun save(qqMusicEntity: QqMusicEntity): QqMusicEntity
    fun delete(qqMusicEntity: QqMusicEntity)
    fun findAll(): List<QqMusicEntity>
}


@Service
class QqMusicServiceImpl: QqMusicService{

    @Resource
    private lateinit var qqMusicRepository: QqMusicRepository

    override fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity? {
        return qqMusicRepository.findByQqEntity(qqEntity)
    }

    override fun save(qqMusicEntity: QqMusicEntity): QqMusicEntity {
        return qqMusicRepository.save(qqMusicEntity)
    }

    override fun delete(qqMusicEntity: QqMusicEntity) {
        qqMusicRepository.delete(qqMusicEntity)
    }

    override fun findAll(): List<QqMusicEntity> {
        return qqMusicRepository.findAll()
    }
}