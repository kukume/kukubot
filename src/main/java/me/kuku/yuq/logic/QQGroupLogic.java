package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.pojo.GroupMember;
import me.kuku.yuq.pojo.Result;

import java.util.List;
import java.util.Map;

@AutoBind
public interface QQGroupLogic {
    String addGroupMember(Long qq, Long group);
    String setGroupAdmin(Long qq, Long group, boolean isAdmin);
    String setGroupCard(Long qq, Long group, String name);
    String deleteGroupMember(Long qq, Long group, boolean isFlag);
    String addHomeWork(Long group, String courseName, String title, String content, boolean needFeedback);
    String groupCharin(Long group, String content, Long time);
    Result<List<Map<String, String>>> groupLevel(Long group);
    Result<GroupMember> queryMemberInfo(Long group, Long qq);
    Result<List<String>> essenceMessage(Long group);
    Result<List<Long>> queryGroup();
    List<Map<String, String>> groupHonor(Long group, String type);
    Result<String> groupSign(Long group, String place, String text, String name, String picId, String picUrl);
}
