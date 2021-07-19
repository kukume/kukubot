package me.kuku.simbot.logic;

import me.kuku.pojo.Result;
import me.kuku.simbot.entity.QqLoginEntity;
import me.kuku.simbot.entity.QqVideoEntity;
import me.kuku.simbot.pojo.GroupMember;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface QqLoginLogic {
	Result<Map<String, String>> groupUploadImage(QqLoginEntity QqLoginEntity, String url) throws IOException;
	String vipSign(QqLoginEntity QqLoginEntity) throws IOException;
	String queryVip(QqLoginEntity QqLoginEntity) throws IOException;
	String yellowSign(QqLoginEntity QqLoginEntity) throws IOException;
	String qqVideoSign1(QqLoginEntity QqLoginEntity) throws IOException;
	String qqVideoSign2(QqLoginEntity QqLoginEntity) throws IOException;
	String bigVipSign(QqLoginEntity QqLoginEntity) throws IOException;
	String weiYunSign(QqLoginEntity QqLoginEntity) throws IOException;
	String qqMusicSign(QqLoginEntity QqLoginEntity) throws IOException;
	String gameSign(QqLoginEntity QqLoginEntity) throws IOException;
	String qPetSign(QqLoginEntity QqLoginEntity) throws IOException;
	String blueSign(QqLoginEntity QqLoginEntity) throws IOException;
	String sendFlower(QqLoginEntity QqLoginEntity, Long qq, Long group) throws IOException;
	String diyBubble(QqLoginEntity QqLoginEntity, String text, String name) throws IOException;
	String vipGrowthAdd(QqLoginEntity QqLoginEntity) throws IOException;
	String publishNotice(QqLoginEntity QqLoginEntity, Long group, String text) throws IOException;
	String getGroupLink(QqLoginEntity QqLoginEntity, Long group) throws IOException;
	String groupActive(QqLoginEntity QqLoginEntity, Long group, Integer page) throws IOException;
	String allShutUp(QqLoginEntity QqLoginEntity, Long group, Boolean isShutUp) throws IOException;
	String changeName(QqLoginEntity QqLoginEntity, Long qq, Long group, String name) throws IOException;
	String setGroupAdmin(QqLoginEntity QqLoginEntity, Long qq, Long group, Boolean isAdmin) throws IOException;
	String growthLike(QqLoginEntity QqLoginEntity) throws IOException;
	Result<List<GroupMember>> groupMemberInfo(QqLoginEntity QqLoginEntity, Long group) throws IOException;
	String changePhoneOnline(QqLoginEntity QqLoginEntity, String iMei, String phone) throws IOException;
	String queryLevel(QqLoginEntity QqLoginEntity, Long qq, String psKey) throws IOException;
	List<Map<String, String>> getGroupMsgList(QqLoginEntity QqLoginEntity) throws IOException;
	String operatingGroupMsg(QqLoginEntity QqLoginEntity, String type, Map<String, String> map, String refuseMsg) throws IOException;
	Result<String> videoSign(QqVideoEntity qqVideoEntity) throws IOException;
}
