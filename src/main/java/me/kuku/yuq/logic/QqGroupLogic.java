package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import com.alibaba.fastjson.JSONArray;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.QqLoginEntity;
import me.kuku.yuq.pojo.GroupMember;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@AutoBind
public interface QqGroupLogic {
	String addGroupMember(QqLoginEntity qqLoginEntity, Long qq, Long group) throws IOException;
	String setGroupAdmin(QqLoginEntity qqLoginEntity, Long qq, Long group, boolean isAdmin) throws IOException;
	String setGroupCard(QqLoginEntity qqLoginEntity, Long qq, Long group, String name) throws IOException;
	String deleteGroupMember(QqLoginEntity qqLoginEntity, Long qq, Long group, boolean isFlag) throws IOException;
	String addHomeWork(QqLoginEntity qqLoginEntity, Long group, String courseName, String title, String content, boolean needFeedback) throws IOException;
	String groupCharin(QqLoginEntity qqLoginEntity, Long group, String content, Long time) throws IOException;
	Result<List<Map<String, String>>> groupLevel(QqLoginEntity qqLoginEntity, Long group) throws IOException;
	Result<GroupMember> queryMemberInfo(QqLoginEntity qqLoginEntity, Long group, Long qq) throws IOException;
	Result<List<JSONArray>> essenceMessage(QqLoginEntity qqLoginEntity, Long group) throws IOException;
	Result<List<Long>> queryGroup(QqLoginEntity qqLoginEntity) throws IOException;
	List<Map<String, String>> groupHonor(QqLoginEntity qqLoginEntity, Long group, String type) throws IOException;
//    Result<String> groupSign(QqLoginEntity qqLoginEntity, Long group, String place, String text, String name, String picId, String picUrl) throws IOException;
}
