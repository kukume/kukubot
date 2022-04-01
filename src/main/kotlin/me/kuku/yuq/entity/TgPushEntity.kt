package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "tg_push")
class TgPushEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(unique = true, name = "key_")
    var key: String = ""
    @Column(unique = true)
    var userid: Long = 0
}

interface TgPushRepository: JpaRepository<TgPushEntity, Int> {
    fun findByUserid(userid: Long): TgPushEntity?
    fun findByKey(key: String): TgPushEntity?
}

class TgPushService @Inject constructor(
    private val tgPushRepository: TgPushRepository
){
    fun findByUserid(userid: Long) = tgPushRepository.findByUserid(userid)

    fun findByKey(key: String) = tgPushRepository.findByKey(key)

    fun save(tgPushEntity: TgPushEntity): TgPushEntity = tgPushRepository.save(tgPushEntity)
}