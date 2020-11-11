package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.YuDao;
import com.icecreamqaq.yudb.jpa.annotation.Dao;
import me.kuku.yuq.entity.MessageEntity;

@Dao
public interface MessageDao extends YuDao<MessageEntity, Long> {
    MessageEntity findByMessageId(int messageId);
}