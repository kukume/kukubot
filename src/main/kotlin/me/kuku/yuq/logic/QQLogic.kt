package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.GroupMember

@AutoBind
interface QQLogic {
    //群签到
    fun groupSign(qqEntity: QQEntity, group: Long, place: String, text: String, info: String, url: String? = null): String
    //群抽礼物
    fun groupLottery(qqEntity: QQEntity, group: Long): String
    //会员签到
    fun vipSign(qqEntity: QQEntity): String
    //查询业务
    fun queryVip(qqEntity: QQEntity): String
    //游戏中心启动
    fun phoneGameSign(qqEntity: QQEntity): String
    //黄钻签到
    fun yellowSign(qqEntity: QQEntity): String
    //腾讯视频签到
    fun qqVideoSign1(qqEntity: QQEntity): String
    fun qqVideoSign2(qqEntity: QQEntity): String
    //svip早起报名
    fun sVipMornSign(qqEntity: QQEntity): String
    //svip早起打卡
    fun sVipMornClock(qqEntity: QQEntity): String
    //大会员签到
    fun bigVipSign(qqEntity: QQEntity): String
    //改昵称
    fun modifyNickname(qqEntity: QQEntity, nickname: String): String
    //改头像
    fun modifyAvatar(qqEntity: QQEntity, url: String): String
    //微云签到
    fun weiYunSign(qqEntity: QQEntity): String
    //QQ音乐签到
    fun qqMusicSign(qqEntity: QQEntity): String
    //游戏签到
    fun gameSign(qqEntity: QQEntity): String
    //Q宠签到
    fun qPetSign(qqEntity: QQEntity): String
    //兴趣部落签到
    fun tribeSign(qqEntity: QQEntity): String
    //拒绝任何人添加
    fun refuseAdd(qqEntity: QQEntity): String
    //运动签到
    fun motionSign(qqEntity: QQEntity): String
    //蓝钻签到
    fun blueSign(qqEntity: QQEntity): String
    //赞名片
    fun like(qqEntity: QQEntity, qq: Long, psKey: String? = null): String
    //送花
    fun sendFlower(qqEntity: QQEntity, qq: Long, group: Long): String
    //收集卡打卡
    fun anotherSign(qqEntity: QQEntity): String
    //diy气泡
    fun diyBubble(qqEntity: QQEntity, text: String, name: String?): String
    //打卡
    fun qqSign(qqEntity: QQEntity): String
    //查询vip成长值
    fun vipGrowthAdd(qqEntity: QQEntity): String
    //发公告
    fun publishNotice(qqEntity: QQEntity, group: Long, text: String): String
    //获取加群链接
    fun getGroupLink(qqEntity: QQEntity, group: Long): String
    //获取群活跃信息
    fun groupActive(qqEntity: QQEntity, group: Long, page: Int): String
    //微视签到
    fun weiShiSign(qqEntity: QQEntity): String
    //获取群文件链接
    fun groupFileUrl(qqEntity: QQEntity, group: Long, folderName: String?): String
    //全体禁言
    fun allShutUp(qqEntity: QQEntity, group: Long, isShutUp: Boolean): String
    //改群昵称
    fun changeName(qqEntity: QQEntity, qq: Long, group: Long, name: String): String
    //设置管理员
    fun setGroupAdmin(qqEntity: QQEntity, qq: Long, group: Long, isAdmin: Boolean): String
    //成长值排行榜点赞
    fun growthLike(qqEntity: QQEntity): String
    //查询群成员信息
    fun groupMemberInfo(qqEntity: QQEntity, group: Long): CommonResult<List<GroupMember>>
    //设置自定义机型在线
    fun changePhoneOnline(qqEntity: QQEntity, iMei:String, phone: String): String
    //删除群文件
    fun removeGroupFile(qqEntity: QQEntity, group: Long, fileName: String, folderName: String?): String
    //查询好友业务
    fun queryFriendVip(qqEntity: QQEntity, qq: Long, psKey: String?): String
    //查询等级等信息
    fun queryLevel(qqEntity: QQEntity, qq: Long, psKey: String?): String
}