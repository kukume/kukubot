package me.kuku.yuq.dao;

import com.icecreamqaq.yudb.jpa.JPADao;
import com.icecreamqaq.yudb.jpa.annotation.Select;
import me.kuku.yuq.entity.MessageEntity;

import java.util.List;

public interface MessageDao extends JPADao<MessageEntity, Long> {
    MessageEntity findByMessageId(int messageId);
    @Select("from MessageEntity where QQ = ?0 and GROUP_ = ?1 order by date desc")
    List<MessageEntity> findByQQAndGroup(Long qq, Long group);
    @Select("from MessageEntity where group_ = ?0 and qq <> ?1 order by id desc")
    List<MessageEntity> findByGroupExcludeQQ(Long group, long qq);
}