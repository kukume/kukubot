package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "weibo")
class WeiboEntity {
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
}

interface WeiboRepository: JpaRepository<WeiboEntity, Int> {
    fun findByQqEntity(qqEntity: QqEntity): WeiboEntity?
}

class WeiboService @Inject constructor(
    private val weiboRepository: WeiboRepository
) {

    fun save(weiboEntity: WeiboEntity): WeiboEntity = weiboRepository.save(weiboEntity)

    fun findAll(): List<WeiboEntity> = weiboRepository.findAll()

    fun findByUserEntity(qqEntity: QqEntity) = weiboRepository.findByQqEntity(qqEntity)

}