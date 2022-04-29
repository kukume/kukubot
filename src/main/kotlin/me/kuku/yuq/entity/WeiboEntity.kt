package me.kuku.yuq.entity

import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "weibo")
class WeiboEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 2000)
    var pcCookie: String = ""
    @Column(length = 2000)
    var mobileCookie: String= ""
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var config: WeiboConfig = WeiboConfig()
}

interface WeiboRepository: JpaRepository<WeiboEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): WeiboEntity?
}

@Service
class WeiboService (
    private val weiboRepository: WeiboRepository
) {

    fun save(weiboEntity: WeiboEntity): WeiboEntity = weiboRepository.save(weiboEntity)

    fun findAll(): List<WeiboEntity> = weiboRepository.findAll()

    fun findByQqEntity(qqEntity: QqEntity) = weiboRepository.findByQqEntity(qqEntity)

}

data class WeiboConfig(var push: Status = Status.OFF, var sign: Status = Status.OFF)