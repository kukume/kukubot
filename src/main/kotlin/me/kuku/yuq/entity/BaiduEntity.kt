package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "baidu")
class BaiduEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 2000)
    var cookie: String = ""
    var bdUss: String = ""
    var sToken: String = ""
    var tieBaSToken: String = ""


    fun otherCookie(sToken: String): String {
        return cookie.replace("STOKEN=" + this.sToken + "; ", "STOKEN=$sToken; ")
    }

    fun teiBaCookie(): String {
        return otherCookie(tieBaSToken)
    }
}

interface BaiduRepository: JpaRepository<BaiduEntity, Int>, QuerydslPredicateExecutor<BaiduEntity> {
    fun findByQqEntity(qqEntity: QqEntity): BaiduEntity?
}

class BaiduService @Inject constructor(
    private val baiduRepository: BaiduRepository
) {

    fun findByQqEntity(qqEntity: QqEntity) = baiduRepository.findByQqEntity(qqEntity)

    fun save(baiduEntity: BaiduEntity): BaiduEntity = baiduRepository.save(baiduEntity)

    fun delete(baiduEntity: BaiduEntity) = baiduRepository.delete(baiduEntity)

    fun findByUserid(id: Int): BaiduEntity? {
        with(QBaiduEntity.baiduEntity) {
            return baiduRepository.findOne(qqEntity.id.eq(id)).orElse(null)
        }
    }

}