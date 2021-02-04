package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.pojo.GroupMember;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@AutoBind
public interface QQLoginLogic {
    Result<Map<String, String>> groupUploadImage(QQLoginEntity qqLoginEntity, String url) throws IOException;
    String vipSign(QQLoginEntity qqLoginEntity) throws IOException;
    String queryVip(QQLoginEntity qqLoginEntity) throws IOException;
    String phoneGameSign(QQLoginEntity qqLoginEntity) throws IOException;
    String yellowSign(QQLoginEntity qqLoginEntity) throws IOException;
    String qqVideoSign1(QQLoginEntity qqLoginEntity) throws IOException;
    String qqVideoSign2(QQLoginEntity qqLoginEntity) throws IOException;
    String sVipMornSign(QQLoginEntity qqLoginEntity) throws IOException;
    String sVipMornClock(QQLoginEntity qqLoginEntity) throws IOException;
    String bigVipSign(QQLoginEntity qqLoginEntity) throws IOException;
    String modifyNickname(QQLoginEntity qqLoginEntity, String nickname) throws IOException;
    String modifyAvatar(QQLoginEntity qqLoginEntity, String url) throws IOException;
    String weiYunSign(QQLoginEntity qqLoginEntity) throws IOException;
    String qqMusicSign(QQLoginEntity qqLoginEntity) throws IOException;
    String gameSign(QQLoginEntity qqLoginEntity) throws IOException;
    String qPetSign(QQLoginEntity qqLoginEntity) throws IOException;
    String tribeSign(QQLoginEntity qqLoginEntity) throws IOException;
    String refuseAdd(QQLoginEntity qqLoginEntity) throws IOException;
    String motionSign(QQLoginEntity qqLoginEntity) throws IOException;
    String blueSign(QQLoginEntity qqLoginEntity) throws IOException;
    String sendFlower(QQLoginEntity qqLoginEntity, Long qq, Long group) throws IOException;
    String anotherSign(QQLoginEntity qqLoginEntity) throws IOException;
    String diyBubble(QQLoginEntity qqLoginEntity, String text, String name) throws IOException;
    String qqSign(QQLoginEntity qqLoginEntity) throws IOException;
    String vipGrowthAdd(QQLoginEntity qqLoginEntity) throws IOException;
    String publishNotice(QQLoginEntity qqLoginEntity, Long group, String text) throws IOException;
    String getGroupLink(QQLoginEntity qqLoginEntity, Long group) throws IOException;
    String groupActive(QQLoginEntity qqLoginEntity, Long group, Integer page) throws IOException;
    String weiShiSign(QQLoginEntity qqLoginEntity) throws IOException;
    String groupFileUrl(QQLoginEntity qqLoginEntity, Long group, String folderName) throws IOException;
    String allShutUp(QQLoginEntity qqLoginEntity, Long group, Boolean isShutUp) throws IOException;
    String changeName(QQLoginEntity qqLoginEntity, Long qq, Long group, String name) throws IOException;
    String setGroupAdmin(QQLoginEntity qqLoginEntity, Long qq, Long group, Boolean isAdmin) throws IOException;
    String growthLike(QQLoginEntity qqLoginEntity) throws IOException;
    Result<List<GroupMember>> groupMemberInfo(QQLoginEntity qqLoginEntity, Long group) throws IOException;
    String changePhoneOnline(QQLoginEntity qqLoginEntity, String iMei, String phone) throws IOException;
    String removeGroupFile(QQLoginEntity qqLoginEntity, Long group, String fileName, String folderName) throws IOException;
    String queryFriendVip(QQLoginEntity qqLoginEntity, Long qq, String psKey) throws IOException;
    String queryLevel(QQLoginEntity qqLoginEntity, Long qq, String psKey) throws IOException;
    List<Map<String, String>> getGroupMsgList(QQLoginEntity qqLoginEntity) throws IOException;
    String operatingGroupMsg(QQLoginEntity qqLoginEntity, String type, Map<String, String> map, String refuseMsg) throws IOException;
}
