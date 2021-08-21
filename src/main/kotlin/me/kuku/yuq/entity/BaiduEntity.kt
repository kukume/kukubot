package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "baidu")
data class BaiduEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 2000)
    var cookie: String = "",
    var bdUss: String = "",
    var sToken: String = "",
    var tieBaSToken: String? = ""
){
    @Transient
    fun getOtherCookie(sToken: String): String{
        return cookie.replace("STOKEN=${this.sToken}; ", "STOKEN=$sToken; ")
    }

    @Transient
    fun getTieBaCookie(): String{
        return getOtherCookie(tieBaSToken ?: "");
    }
}

interface BaiduDao: JPADao<BaiduEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): BaiduEntity?
}

@AutoBind
interface BaiduService{
    fun findByQqEntity(qqEntity: QqEntity): BaiduEntity?
    fun save(baiduEntity: BaiduEntity)
    fun delete(baiduEntity: BaiduEntity)
    fun findAll(): List<BaiduEntity>
}

class BaiduServiceImpl: BaiduService{
    @Inject
    private lateinit var baiduDao: BaiduDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): BaiduEntity? {
        return baiduDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun save(baiduEntity: BaiduEntity) {
        return baiduDao.saveOrUpdate(baiduEntity)
    }

    @Transactional
    override fun delete(baiduEntity: BaiduEntity) {
        return baiduDao.delete(baiduEntity.id!!)
    }

    @Transactional
    override fun findAll(): List<BaiduEntity> {
        return baiduDao.findAll()
    }
}

