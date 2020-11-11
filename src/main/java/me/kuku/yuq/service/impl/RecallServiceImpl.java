package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.RecallDao;
import me.kuku.yuq.entity.RecallEntity;
import me.kuku.yuq.service.RecallService;

import javax.inject.Inject;
import java.util.List;

public class RecallServiceImpl implements RecallService {
    @Inject
    private RecallDao recallDao;
    @Override
    @Transactional
    public List<RecallEntity> findByGroupAndQQ(Long group, Long qq) {
        return recallDao.findByGroupAndQQ(group, qq);
    }

    @Override
    @Transactional
    public void save(RecallEntity recallEntity) {
        recallDao.saveOrUpdate(recallEntity);
    }
}
