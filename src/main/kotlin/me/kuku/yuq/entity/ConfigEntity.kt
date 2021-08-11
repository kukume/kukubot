package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Execute
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "config")
data class ConfigEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @Column(unique = true)
    var type: String = "",
    @Column(length = 20000)
    var content: String = ""
){
    companion object{
        fun getInstance(type: String): ConfigEntity{
            return ConfigEntity(type = type)
        }
    }

    var contentJsonObject: JSONObject
        get() = JSON.parseObject(content)
        set(value) {
            content = value.toJSONString()
        }

    var contentJsonArray: JSONArray
        get() = JSON.parseArray(content)
        set(value) {
            content = value.toJSONString()
        }

    fun <T> getConfigParse(clazz: Class<T>): T {
        return if (content.isEmpty()) {
            clazz.newInstance()
        } else JSON.parseObject<T>(content, clazz)
    }

    fun <T> setConfigParse(t: T) {
        content = JSON.toJSONString(t)
    }
}

interface ConfigDao: JPADao<ConfigEntity, Int>{
    fun findByType(type: String): ConfigEntity?
    @Execute("delete from ConfigEntity where type = ?0")
    fun deleteByType(type: String)
}

@AutoBind
interface ConfigService{
    fun findByType(type: String): ConfigEntity?
    fun findByType(type: ConfigType): ConfigEntity?{
        return findByType(type.type)
    }
    fun save(configEntity: ConfigEntity)
    fun delete(configEntity: ConfigEntity)
    fun deleteByType(type: String)
}


class ConfigServiceImpl @Inject constructor(private val configDao: ConfigDao): ConfigService{
    @Transactional
    override fun findByType(type: String) = configDao.findByType(type)

    @Transactional
    override fun save(configEntity: ConfigEntity) = configDao.saveOrUpdate(configEntity)

    @Transactional
    override fun delete(configEntity: ConfigEntity) = configDao.delete(configEntity.id!!)

    @Transactional
    override fun deleteByType(type: String) = configDao.deleteByType(type)
}

enum class ConfigType(val type: String){
    BAIDU_AI("baiduAi"),
    TU_LING("tuLing")
}