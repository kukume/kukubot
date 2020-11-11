package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@AutoBind
public interface QQZoneLogic {
    List<Map<String, String>> friendTalk(QQLoginEntity qqLoginEntity) throws IOException;
    List<Map<String, String>> talkByQQ(QQLoginEntity qqLoginEntity, Long qq) throws IOException;
    String forwardTalk(QQLoginEntity qqLoginEntity, String id, String qq, String text) throws IOException;
    String publishTalk(QQLoginEntity qqLoginEntity, String text) throws IOException;
    String removeTalk(QQLoginEntity qqLoginEntity, String id) throws IOException;
    String commentTalk(QQLoginEntity qqLoginEntity, String id, String qq, String text) throws IOException;
    String likeTalk(QQLoginEntity qqLoginEntity, Map<String, String> map) throws IOException;
    Result<List<Map<String, String>>> queryGroup(QQLoginEntity qqLoginEntity) throws IOException;
    Result<List<Map<String, String>>> queryGroupMember(QQLoginEntity qqLoginEntity, String group) throws IOException;
    String leaveMessage(QQLoginEntity qqLoginEntity, Long qq, String content) throws IOException;
    String visitQZone(QQLoginEntity qqLoginEntity, Long qq) throws IOException;
}
