package me.kuku.yuq.entity

import me.kuku.utils.OkUtils
import org.hibernate.annotations.Type
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service
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
    @Type(type = "json")
    @Column(columnDefinition = "text")
    var config: BaiduConfig = BaiduConfig()

    fun otherCookie(sToken: String): String {
        return OkUtils.cookieStr(cookie, "BDUSS") + "STOKEN=$sToken; "
    }

    fun teiBaCookie(): String {
        return otherCookie(config.tieBaSToken)
    }
}

interface BaiduRepository: JpaRepository<BaiduEntity, Int>, QuerydslPredicateExecutor<BaiduEntity> {
    fun findByQqEntity(qqEntity: QqEntity): BaiduEntity?
}

@Service
class BaiduService (
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

    fun findAll(): List<BaiduEntity> = baiduRepository.findAll()

}

data class BaiduConfig(var sign: Status = Status.OFF, var tieBaSToken: String = "")