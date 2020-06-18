package me.kuku.yuq.service

import me.kuku.yuq.entity.QQEntity

interface QQZoneService {
    //取好友说说
    fun friendTalk(qqEntity: QQEntity): List<Map<String, String?>>?
    //取自己说说
    fun myTalk(qqEntity: QQEntity): List<Map<String, String?>>?
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
}