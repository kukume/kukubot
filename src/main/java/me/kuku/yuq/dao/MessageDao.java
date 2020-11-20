package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.jpa.JPADao;
import me.kuku.yuq.entity.MessageEntity;

public interface MessageDao extends JPADao<MessageEntity, Long> {
    MessageEntity findByMessageId(int messageId);
}