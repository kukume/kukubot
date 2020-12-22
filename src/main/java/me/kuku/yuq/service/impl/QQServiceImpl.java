package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.GroupDao;
import me.kuku.yuq.dao.QQDao;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.entity.QQEntity;
import me.kuku.yuq.service.QQService;

import javax.inject.Inject;
import java.util.List;

public class QQServiceImpl implements QQService {
    @Inject
    private QQDao qqDao;
    @Inject
    private GroupDao groupDao;
    @Override
    @Transactional
    public QQEntity findByQQAndGroup(Long qq, Long group) {
        GroupEntity groupEntity = groupDao.findByGroup(group);
        if (groupEntity == null) return null;
        return qqDao.findByQQAndGroup(qq, groupEntity.getId());
    }

    @Override
    @Transactional
    public void save(QQEntity qqEntity) {
        qqDao.saveOrUpdate(qqEntity);
    }

    @Override
    @Transactional
    public int delByQQAndGroup(Long qq, Long group) {
        GroupEntity groupEntity = groupDao.findByGroup(group);
        if (groupEntity == null) return 0;
        return qqDao.delByQQAndGroup(qq, groupEntity.getId());
    }

    @Override
    @Transactional
    public List<QQEntity> findAll() {
        return qqDao.findAll();
    }

    @Override
    @Transactional
    public List<QQEntity> findByHostLocPush(boolean hostLocPush) {
        return qqDao.findByHostLocPush(hostLocPush);
    }
}
