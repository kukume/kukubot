package me.kuku.simbot.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.annotation.Resource
import javax.persistence.*

@Entity
@Table(name = "group_")
data class GroupEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Int? = null,
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
    var pic: Boolean? = false,
    var leaveGroupBlack: Boolean? = false,
    var autoReview: Boolean? = false,
    var onTimeAlarm: Boolean? = false,
    var maxCommandCountOnTime: Int? = -1,
    var locMonitor: Boolean? = false,
    var flashNotify: Boolean? = false,
    var repeat: Boolean? = true,
    var groupAdminAuth: Boolean? = false,
    var kickWithoutSpeaking: Boolean? = false,
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
}

interface GroupRepository: JpaRepository<GroupEntity, Int>{
    fun findByGroup(group: Long): GroupEntity?
    fun deleteByGroup(group: Long)
}

interface GroupService{
    fun findByGroup(group: Long): GroupEntity?
    fun save(groupEntity: GroupEntity): GroupEntity
    fun findAll(): List<GroupEntity>
    fun deleteByGroup(group: Long)
    fun delete(groupEntity: GroupEntity)
}

@Service
class GroupServiceImpl: GroupService{

    @Resource
    private lateinit var groupRepository: GroupRepository

    override fun findByGroup(group: Long): GroupEntity? {
        return groupRepository.findByGroup(group)
    }

    override fun save(groupEntity: GroupEntity): GroupEntity {
        return groupRepository.save(groupEntity)
    }

    override fun findAll(): List<GroupEntity> {
        return groupRepository.findAll()
    }

    override fun deleteByGroup(group: Long) {
        return groupRepository.deleteByGroup(group)
    }

    override fun delete(groupEntity: GroupEntity) {
        return groupRepository.delete(groupEntity)
    }
}