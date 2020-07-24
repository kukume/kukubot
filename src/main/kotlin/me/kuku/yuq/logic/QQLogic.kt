package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.GroupMember

@AutoBind
interface QQLogic {
    fun groupSign(qqEntity: QQEntity, group: Long, place: String, text: String, info: String, url: String? = null): String
    fun groupLottery(qqEntity: QQEntity, group: Long): String
    fun vipSign(qqEntity: QQEntity): String
    fun queryVip(qqEntity: QQEntity): String
    fun phoneGameSign(qqEntity: QQEntity): String
    fun yellowSign(qqEntity: QQEntity): String
    fun qqVideoSign1(qqEntity: QQEntity): String
    fun qqVideoSign2(qqEntity: QQEntity): String
    fun sVipMornSign(qqEntity: QQEntity): String
    fun sVipMornClock(qqEntity: QQEntity): String
    fun bigVipSign(qqEntity: QQEntity): String
    fun modifyNickname(qqEntity: QQEntity, nickname: String): String
    fun modifyAvatar(qqEntity: QQEntity, url: String): String
    fun weiYunSign(qqEntity: QQEntity): String
    fun qqMusicSign(qqEntity: QQEntity): String
    fun gameSign(qqEntity: QQEntity): String
    fun qPetSign(qqEntity: QQEntity): String
    fun tribeSign(qqEntity: QQEntity): String
    fun refuseAdd(qqEntity: QQEntity): String
    fun motionSign(qqEntity: QQEntity): String
    fun blueSign(qqEntity: QQEntity): String
    fun like(qqEntity: QQEntity, qq: Long, psKey: String? = null): String
    fun sendFlower(qqEntity: QQEntity, qq: Long, group: Long): String
    fun anotherSign(qqEntity: QQEntity): String
    fun diyBubble(qqEntity: QQEntity, text: String, name: String?): String
    fun qqSign(qqEntity: QQEntity): String
    fun vipGrowthAdd(qqEntity: QQEntity): String
    fun publishNotice(qqEntity: QQEntity, group: Long, text: String): String
    fun getGroupLink(qqEntity: QQEntity, group: Long): String
    fun groupActive(qqEntity: QQEntity, group: Long, page: Int): String
    fun weiShiSign(qqEntity: QQEntity): String
    fun groupFileUrl(qqEntity: QQEntity, group: Long, folderName: String?): String
    fun allShutUp(qqEntity: QQEntity, group: Long, isShutUp: Boolean): String
    fun changeName(qqEntity: QQEntity, qq: Long, group: Long, name: String): String
    fun setGroupAdmin(qqEntity: QQEntity, qq: Long, group: Long, isAdmin: Boolean): String
    fun growthLike(qqEntity: QQEntity): String
    fun groupMemberInfo(qqEntity: QQEntity, group: Long): CommonResult<List<GroupMember>>
    fun changePhoneOnline(qqEntity: QQEntity, iMei:String, phone: String): String
}