package me.kuku.yuq.service.impl;

import com.icecreamqaq.yudb.jpa.annotation.Transactional;
import me.kuku.yuq.dao.MotionDao;
import me.kuku.yuq.entity.StepEntity;
import me.kuku.yuq.service.MotionService;

import javax.inject.Inject;
import java.util.List;

public class MotionServiceImpl implements MotionService {

    @Inject
    private MotionDao motionDao;

    @Override
    @Transactional
    public StepEntity findByQQ(long qq) {
        return motionDao.findByQQ(qq);
    }

    @Override
    @Transactional
    public List<StepEntity> findAll() {
        return motionDao.findAll();
    }

    @Override
    @Transactional
    public void save(StepEntity stepEntity) {
        motionDao.saveOrUpdate(stepEntity);
    }

    @Override
    @Transactional
    public void delByQQ(Long qq) {
        motionDao.delByQQ(qq);
    }
}
