package me.kuku.yuq.service;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.MessageEntity;

import java.util.Map;

@AutoBind
public interface MessageService {
    MessageEntity findByMessageId(int messageId);
    void save(MessageEntity messageEntity);
    Map<Long, Long> findCountQQByGroupAndToday(Long group);
}