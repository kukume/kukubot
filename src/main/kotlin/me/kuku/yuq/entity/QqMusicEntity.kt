package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "qq_music")
data class QqMusicEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @OneToOne
    @JoinColumn(name = "qq")
    var qqEntity: QqEntity? = null,
    @Column(length = 1000)
    var cookie: String = "",
    var qqMusicKey: String? = "",
    var convertGreenDiamond: Boolean? = false,
    var autoComment: Boolean? = false,
    var autoPublishView: Boolean? = false
)

interface QqMusicDao: JPADao<QqMusicEntity, Int>{
    fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity?
    fun findByConvertGreenDiamond(convertGreenDiamond: Boolean): List<QqMusicEntity>
    fun findByAutoComment(autoComment: Boolean): List<QqMusicEntity>
    fun findByAutoPublishView(autoPublishView: Boolean): List<QqMusicEntity>
}

@AutoBind
interface QqMusicService {
    fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity?
    fun save(qqMusicEntity: QqMusicEntity)
    fun delete(qqMusicEntity: QqMusicEntity)
    fun findAll(): List<QqMusicEntity>
    fun findByConvertGreenDiamond(convertGreenDiamond: Boolean): List<QqMusicEntity>
    fun findByAutoComment(autoComment: Boolean): List<QqMusicEntity>
    fun findByAutoPublishView(autoPublishView: Boolean): List<QqMusicEntity>
}

class QqMusicServiceImpl: QqMusicService{

    @Inject
    private lateinit var qqMusicDao: QqMusicDao

    @Transactional
    override fun findByQqEntity(qqEntity: QqEntity): QqMusicEntity? {
        return qqMusicDao.findByQqEntity(qqEntity)
    }

    @Transactional
    override fun save(qqMusicEntity: QqMusicEntity) {
        return qqMusicDao.saveOrUpdate(qqMusicEntity)
    }

    @Transactional
    override fun delete(qqMusicEntity: QqMusicEntity) {
        qqMusicDao.delete(qqMusicEntity.id!!)
    }

    @Transactional
    override fun findAll(): List<QqMusicEntity> {
        return qqMusicDao.findAll()
    }

    @Transactional
    override fun findByConvertGreenDiamond(convertGreenDiamond: Boolean): List<QqMusicEntity> {
        return qqMusicDao.findByConvertGreenDiamond(convertGreenDiamond)
    }

    override fun findByAutoComment(autoComment: Boolean): List<QqMusicEntity> {
        return qqMusicDao.findByAutoComment(autoComment)
    }

    override fun findByAutoPublishView(autoPublishView: Boolean): List<QqMusicEntity> {
        return qqMusicDao.findByAutoPublishView(autoPublishView)
    }
}