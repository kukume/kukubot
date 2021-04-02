package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.MessageDao;
import me.kuku.yuq.entity.MessageEntity;
import me.kuku.yuq.service.MessageService;
import me.kuku.yuq.utils.DateTimeFormatterUtils;

import javax.inject.Inject;
import java.util.*;

public class MessageServiceImpl implements MessageService {

    @Inject
    private MessageDao messageDao;

    @Override
    public MessageEntity findByMessageId(int messageId) {
        return messageDao.findByMessageId(messageId);
    }

    @Override
    @Transactional
    public void save(MessageEntity messageEntity) {
        messageDao.saveOrUpdate(messageEntity);
    }

    @Override
    @Transactional
    public Map<Long, Long> findCountQQByGroupAndToday(Long group) {
        String today = DateTimeFormatterUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");
        List<?> result = messageDao.query(
                "select count(qq),qq from MessageEntity where group_ = ?0 and date > parsedatetime('" + today + "', 'yyyy-MM-dd') group by qq order by count(qq) desc",
                group
        ).list();
        Map<Long, Long> map = new HashMap<>();
        for (Object o : result) {
            Object[] objArr = (Object[]) o;
            map.put(Long.parseLong(objArr[1].toString()),
                    Long.parseLong(objArr[0].toString()));
        }
        return map;
    }

    @Override
    public List<MessageEntity> findLastMessage(Long qq, Long group) {
        return messageDao.findByQQAndGroup(qq, group);
    }

    @Override
    public List<MessageEntity> findByGroupExcludeQQ(Long group, Long qq) {
        return messageDao.findByGroupExcludeQQ(group, qq);
    }

    @Override
    public List<MessageEntity> findByQQAndGroup(Long qq, Long group) {
        return messageDao.findByQQAndGroup(qq, group);
    }

    @Override
    public int findCountByQQAndGroupAndToday(long qq, long group) {
        String today = DateTimeFormatterUtils.format(System.currentTimeMillis(), "yyyy-MM-dd");
        Object o= messageDao.query(
                "select count(*) from MessageEntity where qq = ?0 and group_ = ?1 and date > parsedatetime('" + today + "', 'yyyy-MM-dd')",
                qq, group
        ).getSingleResult();
        return Integer.parseInt(o.toString());
    }
}
