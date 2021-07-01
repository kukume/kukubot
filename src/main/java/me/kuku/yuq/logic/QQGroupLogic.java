package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import com.alibaba.fastjson.JSONArray;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.pojo.GroupMember;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@AutoBind
public interface QQGroupLogic {
    String addGroupMember(QQLoginEntity qqLoginEntity, Long qq, Long group) throws IOException;
    String setGroupAdmin(QQLoginEntity qqLoginEntity, Long qq, Long group, boolean isAdmin) throws IOException;
    String setGroupCard(QQLoginEntity qqLoginEntity, Long qq, Long group, String name) throws IOException;
    String deleteGroupMember(QQLoginEntity qqLoginEntity, Long qq, Long group, boolean isFlag) throws IOException;
    String addHomeWork(QQLoginEntity qqLoginEntity, Long group, String courseName, String title, String content, boolean needFeedback) throws IOException;
    String groupCharin(QQLoginEntity qqLoginEntity, Long group, String content, Long time) throws IOException;
    Result<List<Map<String, String>>> groupLevel(QQLoginEntity qqLoginEntity, Long group) throws IOException;
    Result<GroupMember> queryMemberInfo(QQLoginEntity qqLoginEntity, Long group, Long qq) throws IOException;
    Result<List<JSONArray>> essenceMessage(QQLoginEntity qqLoginEntity, Long group) throws IOException;
    Result<List<Long>> queryGroup(QQLoginEntity qqLoginEntity) throws IOException;
    List<Map<String, String>> groupHonor(QQLoginEntity qqLoginEntity, Long group, String type) throws IOException;
//    Result<String> groupSign(QQLoginEntity qqLoginEntity, Long group, String place, String text, String name, String picId, String picUrl) throws IOException;
}
