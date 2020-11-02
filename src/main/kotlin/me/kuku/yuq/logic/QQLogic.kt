package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.entity.QQLoginEntity
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.pojo.GroupMember

@AutoBind
interface QQLogic {
    //群上传图片
    fun groupUploadImage(qqLoginEntity: QQLoginEntity, url: String): CommonResult<Map<String, String>>
    //群抽礼物
    fun groupLottery(qqLoginEntity: QQLoginEntity, group: Long): String
    //会员签到
    fun vipSign(qqLoginEntity: QQLoginEntity): String
    //查询业务
    fun queryVip(qqLoginEntity: QQLoginEntity): String
    //游戏中心启动
    fun phoneGameSign(qqLoginEntity: QQLoginEntity): String
    //黄钻签到
    fun yellowSign(qqLoginEntity: QQLoginEntity): String
    //腾讯视频签到
    fun qqVideoSign1(qqLoginEntity: QQLoginEntity): String
    fun qqVideoSign2(qqLoginEntity: QQLoginEntity): String
    //svip早起报名
    fun sVipMornSign(qqLoginEntity: QQLoginEntity): String
    //svip早起打卡
    fun sVipMornClock(qqLoginEntity: QQLoginEntity): String
    //大会员签到
    fun bigVipSign(qqLoginEntity: QQLoginEntity): String
    //改昵称
    fun modifyNickname(qqLoginEntity: QQLoginEntity, nickname: String): String
    //改头像
    fun modifyAvatar(qqLoginEntity: QQLoginEntity, url: String): String
    //微云签到
    fun weiYunSign(qqLoginEntity: QQLoginEntity): String
    //QQ音乐签到
    fun qqMusicSign(qqLoginEntity: QQLoginEntity): String
    //游戏签到
    fun gameSign(qqLoginEntity: QQLoginEntity): String
    //Q宠签到
    fun qPetSign(qqLoginEntity: QQLoginEntity): String
    //兴趣部落签到
    fun tribeSign(qqLoginEntity: QQLoginEntity): String
    //拒绝任何人添加
    fun refuseAdd(qqLoginEntity: QQLoginEntity): String
    //运动签到
    fun motionSign(qqLoginEntity: QQLoginEntity): String
    //蓝钻签到
    fun blueSign(qqLoginEntity: QQLoginEntity): String
    //赞名片
    fun like(qqLoginEntity: QQLoginEntity, qq: Long, psKey: String? = null): String
    //送花
    fun sendFlower(qqLoginEntity: QQLoginEntity, qq: Long, group: Long): String
    //收集卡打卡
    fun anotherSign(qqLoginEntity: QQLoginEntity): String
    //diy气泡
    fun diyBubble(qqLoginEntity: QQLoginEntity, text: String, name: String?): String
    //打卡
    fun qqSign(qqLoginEntity: QQLoginEntity): String
    //查询vip成长值
    fun vipGrowthAdd(qqLoginEntity: QQLoginEntity): String
    //发公告
    fun publishNotice(qqLoginEntity: QQLoginEntity, group: Long, text: String): String
    //获取加群链接
    fun getGroupLink(qqLoginEntity: QQLoginEntity, group: Long): String
    //获取群活跃信息
    fun groupActive(qqLoginEntity: QQLoginEntity, group: Long, page: Int): String
    //微视签到
    fun weiShiSign(qqLoginEntity: QQLoginEntity): String
    //获取群文件链接
    fun groupFileUrl(qqLoginEntity: QQLoginEntity, group: Long, folderName: String?): String
    //全体禁言
    fun allShutUp(qqLoginEntity: QQLoginEntity, group: Long, isShutUp: Boolean): String
    //改群昵称
    fun changeName(qqLoginEntity: QQLoginEntity, qq: Long, group: Long, name: String): String
    //设置管理员
    fun setGroupAdmin(qqLoginEntity: QQLoginEntity, qq: Long, group: Long, isAdmin: Boolean): String
    //成长值排行榜点赞
    fun growthLike(qqLoginEntity: QQLoginEntity): String
    //查询群成员信息
    fun groupMemberInfo(qqLoginEntity: QQLoginEntity, group: Long): CommonResult<List<GroupMember>>
    //设置自定义机型在线
    fun changePhoneOnline(qqLoginEntity: QQLoginEntity, iMei:String, phone: String): String
    //删除群文件
    fun removeGroupFile(qqLoginEntity: QQLoginEntity, group: Long, fileName: String, folderName: String?): String
    //查询好友业务
    fun queryFriendVip(qqLoginEntity: QQLoginEntity, qq: Long, psKey: String?): String
    //查询等级等信息
    fun queryLevel(qqLoginEntity: QQLoginEntity, qq: Long, psKey: String?): String
    //获取通知消息
    fun getGroupMsgList(qqLoginEntity: QQLoginEntity): List<Map<String, String>>
    //操作群通知信息
    fun operatingGroupMsg(qqLoginEntity: QQLoginEntity, type: String, map: Map<String, String>, refuseMsg: String?): String
}