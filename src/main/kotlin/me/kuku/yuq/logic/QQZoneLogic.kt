package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.GroupMember

@AutoBind
interface QQZoneLogic {
    //取所有好友说说
    fun friendTalk(qqEntity: QQEntity): List<Map<String, String?>>?
    //取某人说说
    fun talkByQQ(qqEntity: QQEntity, qq: Long): List<Map<String, String?>>?
    //转发说说
    fun forwardTalk(qqEntity: QQEntity, id: String, qq: String, text: String = ""): String
    //发布说说
    fun publishTalk(qqEntity: QQEntity, text: String): String
    //删除说说
    fun removeTalk(qqEntity: QQEntity, id: String): String
    //评论说说
    fun commentTalk(qqEntity: QQEntity, id: String, qq: String, text: String): String
    //点赞说说
    fun likeTalk(qqEntity: QQEntity, map: Map<String, String?>): String
    //发送好友请求   qq号，验证消息，备注，分组名字
    fun addFriend(qqEntity: QQEntity, qq: Long, msg: String, realName: String?, group: String?): String
    //获取所有的群
    fun queryGroup(qqEntity: QQEntity): CommonResult<List<Map<String, String>>>
    //获取群人数
    fun queryGroupMember(qqEntity: QQEntity, group: String): CommonResult<List<Map<String, String>>>
    //留言
    fun leaveMessage(qqEntity: QQEntity, qq: Long, content: String): String
    //访问空间
    fun visitQZone(qqEntity: QQEntity, qq: Long): String
    fun visitQZoneMobile(qqEntity: QQEntity, qq: Long): CommonResult<GroupMember>
}