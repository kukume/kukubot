package me.kuku.simbot.service.impl;

import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.WeiboEntity;
import me.kuku.simbot.repository.WeiboRepository;
import me.kuku.simbot.service.WeiboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeiboServiceImpl implements WeiboService {

	@Autowired
	private WeiboRepository weiboRepository;

	@Override
	public WeiboEntity save(WeiboEntity weiboEntity) {
		return weiboRepository.save(weiboEntity);
	}

	@Override
	public WeiboEntity findByQqEntity(QqEntity qqEntity) {
		return weiboRepository.findByQqEntity(qqEntity);
	}

	@Override
	public List<WeiboEntity> findByMonitor(Boolean monitor) {
		return weiboRepository.findByMonitor(monitor);
	}
}
