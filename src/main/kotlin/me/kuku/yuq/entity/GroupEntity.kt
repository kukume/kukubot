package me.kuku.yuq.entity

import com.IceCreamQAQ.Yu.annotation.AutoBind
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.icecreamqaq.yudb.jpa.JPADao
import com.icecreamqaq.yudb.jpa.annotation.Transactional
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "group_")
data class GroupEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    @Column(unique = true, name = "group_")
    var group: Long = 0L,
    var status: Boolean? = false,
    @ManyToMany(mappedBy = "groups")
    var qqEntities: Set<QqEntity> = HashSet(),
    @Lob
    @Column(columnDefinition = "text")
    var black: String? = "[]",
    @Lob
    @Column(columnDefinition = "text")
    var violation: String? = "[]",
    @Lob
    @Column(columnDefinition = "text")
    var qa: String? = "[]",
    @Lob
    @Column(columnDefinition = "text")
    var admin: String? = "[]",
    @Lob
    @Column(columnDefinition = "text")
    var weibo: String? = "[]",
    @Lob
    @Column(columnDefinition = "text")
    var biliBili: String? = "[]",
    @Lob
    @Column(columnDefinition = "text")
    var biliBiliLive: String? = "[]",
    @Lob
    @Column(columnDefinition = "text")
    var intercept: String? = "[]",
    @Lob
    @Column(columnDefinition = "text")
    var commandLimit: String? = "{}",
    @Lob
    @Column(columnDefinition = "text")
    var shellCommand: String? = "[]",
    var colorPic: Boolean? = false,
    var recall: Boolean? = false,
    var pornImage: Boolean? = false,
    var leaveGroupBlack: Boolean? = false,
    var autoReview: Boolean? = false,
    var onTimeAlarm: Boolean? = false,
    var maxCommandCountOnTime: Int? = -1,
    var locMonitor: Boolean? = false,
    var flashNotify: Boolean? = false,
    var repeat: Boolean? = true,
    var groupAdminAuth: Boolean? = false,
    var kickWithoutSpeaking: Boolean? = false,
    var githubPush: Boolean? = false,
    var biBiliBiliAtAll: Boolean? = false
){
    companion object{
        fun getInstance(group: Long): GroupEntity{
            return GroupEntity(group = group)
        }
    }

    override fun toString(): String {
        return "";
    }

    override fun hashCode(): Int {
        return 0
    }

    fun getQq(qq: Long): QqEntity? {
        for (qqEntity in qqEntities) {
            if (qqEntity.qq == qq) return qqEntity
        }
        return null
    }

    var blackJson: JSONArray
        get() = if (black == null) JSONArray() else JSON.parseArray(black)
        set(json) {
            black = json.toJSONString()
        }
    var violationJson: JSONArray
        get() = if (violation == null) JSONArray() else JSON.parseArray(violation)
        set(json) {
            violation = json.toJSONString()
        }
    var qaJson: JSONArray
        get() = if (qa == null) JSONArray() else JSON.parseArray(qa)
        set(json) {
            qa = json.toJSONString()
        }
    var adminJson: JSONArray
        get() = if (admin == null) JSONArray() else JSON.parseArray(admin)
        set(json) {
            admin = json.toJSONString()
        }
    var weiboJson: JSONArray
        get() = if (weibo == null) JSONArray() else JSON.parseArray(weibo)
        set(json) {
            weibo = json.toJSONString()
        }
    var biliBiliJson: JSONArray
        get() = if (biliBili == null) JSONArray() else JSON.parseArray(biliBili)
        set(json) {
            biliBili = json.toJSONString()
        }
    var biliBiliLiveJson: JSONArray
        get() = if (biliBiliLive == null) JSONArray() else JSON.parseArray(biliBiliLive)
        set(json) {
            biliBiliLive = json.toJSONString()
        }
    var interceptJson: JSONArray
        get() = if (intercept == null) JSONArray() else JSON.parseArray(intercept)
        set(json) {
            intercept = json.toJSONString()
        }
    var commandLimitJson: JSONObject
        get() = if (commandLimit == null) JSONObject() else JSON.parseObject(commandLimit)
        set(json) {
            commandLimit = json.toJSONString()
        }
    var shellCommandJson: JSONArray
        get() = if (shellCommand == null) JSONArray() else JSON.parseArray(shellCommand)
        set(json) {
            shellCommand = json.toJSONString()
        }
    fun isAdmin(qq: Long): Boolean{
        return adminJson.contains(qq)
    }
}

interface GroupDao: JPADao<GroupEntity, Int>{
    fun findByGroup(group: Long): GroupEntity?
    fun findByGithubPush(githubPush: Boolean): List<GroupEntity>
    fun findByLocMonitor(locMonitor: Boolean): List<GroupEntity>
}

@AutoBind
interface GroupService{
    fun findByGroup(group: Long): GroupEntity?
    fun save(groupEntity: GroupEntity)
    fun delete(id: Int)
    fun findAll(): List<GroupEntity>
    fun findByGithubPush(githubPush: Boolean): List<GroupEntity>
    fun findByLocMonitor(locMonitor: Boolean): List<GroupEntity>
}

class GroupServiceImpl: GroupService{
    @Inject
    private lateinit var groupDao: GroupDao

    @Transactional
    override fun findByGroup(group: Long): GroupEntity? {
        return groupDao.findByGroup(group)
    }

    @Transactional
    override fun save(groupEntity: GroupEntity) {
        return groupDao.saveOrUpdate(groupEntity)
    }

    @Transactional
    override fun delete(id: Int) {
        return groupDao.delete(id)
    }

    @Transactional
    override fun findAll(): List<GroupEntity> {
        return groupDao.findAll()
    }

    @Transactional
    override fun findByGithubPush(githubPush: Boolean): List<GroupEntity> {
        return groupDao.findByGithubPush(githubPush)
    }

    override fun findByLocMonitor(locMonitor: Boolean): List<GroupEntity> {
        return groupDao.findByLocMonitor(locMonitor)
    }
}