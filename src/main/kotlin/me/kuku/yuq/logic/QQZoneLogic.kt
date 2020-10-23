package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQLoginEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.GroupMember

@AutoBind
interface QQZoneLogic {
    //取所有好友说说
    fun friendTalk(qqLoginEntity: QQLoginEntity): List<Map<String, String?>>?
    //取某人说说
    fun talkByQQ(qqLoginEntity: QQLoginEntity, qq: Long): List<Map<String, String?>>?
    //转发说说
    fun forwardTalk(qqLoginEntity: QQLoginEntity, id: String, qq: String, text: String = ""): String
    //发布说说
    fun publishTalk(qqLoginEntity: QQLoginEntity, text: String): String
    //删除说说
    fun removeTalk(qqLoginEntity: QQLoginEntity, id: String): String
    //评论说说
    fun commentTalk(qqLoginEntity: QQLoginEntity, id: String, qq: String, text: String): String
    //点赞说说
    fun likeTalk(qqLoginEntity: QQLoginEntity, map: Map<String, String?>): String
    //发送好友请求   qq号，验证消息，备注，分组名字
    fun addFriend(qqLoginEntity: QQLoginEntity, qq: Long, msg: String, realName: String?, group: String?): String
    //获取所有的群
    fun queryGroup(qqLoginEntity: QQLoginEntity): CommonResult<List<Map<String, String>>>
    //获取群人数
    fun queryGroupMember(qqLoginEntity: QQLoginEntity, group: String): CommonResult<List<Map<String, String>>>
    //留言
    fun leaveMessage(qqLoginEntity: QQLoginEntity, qq: Long, content: String): String
    //访问空间
    fun visitQZone(qqLoginEntity: QQLoginEntity, qq: Long): String
    fun visitQZoneMobile(qqLoginEntity: QQLoginEntity, qq: Long): CommonResult<GroupMember>
}