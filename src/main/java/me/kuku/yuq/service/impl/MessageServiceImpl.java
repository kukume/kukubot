package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import com.icecreamqaq.yudb.jpa.hibernate.HibernateDao;
import me.kuku.yuq.dao.MessageDao;
import me.kuku.yuq.entity.MessageEntity;
import me.kuku.yuq.service.MessageService;
import org.hibernate.Query;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessageServiceImpl implements MessageService {

    @Inject
    private MessageDao messageDao;
    @Inject
    private HibernateDao<MessageEntity, Integer> hibernateDao;

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
        Date date = new Date();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date);
        Query<MessageEntity> query = hibernateDao.query(
                "select count(qq),qq from MessageEntity where group_ = ? and date > parsedatetime('$today', 'yyyy-MM-dd') group by qq order by count(qq) desc",
                group
        );
        List<MessageEntity> result = query.list();
        Map<Long, Long> map = new HashMap<>();
        for (MessageEntity messageEntity: result){
            List<?> list = (List<?>) messageEntity;
            map.put(Long.parseLong(list.get(1).toString()),
                    Long.parseLong(list.get(0).toString()));
        }
        return map;
    }
}
