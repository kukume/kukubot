package me.kuku.yuq.aop;

import com.IceCreamQAQ.Yu.hook.HookMethod;
import com.IceCreamQAQ.Yu.hook.HookRunnable;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.pojo.BaiduAIPojo;
import me.kuku.yuq.service.ConfigService;

import javax.inject.Inject;

@SuppressWarnings("unused")
public class BaiduAIAop implements HookRunnable {

	@Inject
	private ConfigService configService;

	@Override
	public boolean preRun(HookMethod method) {
		Object[] paras = method.paras;
		if (paras.length > 1 && paras[1] instanceof BaiduAIPojo) {
			String ocrAppId = null;
			String ocrAppKey = null;
			String ocrSecretKey = null;
			String contentCensorAppId = null;
			String contentCensorAppKey = null;
			String contentCensorSecretKey = null;
			String speechAppId = null;
			String speechAppKey = null;
			String speechSecretKey = null;
			ConfigEntity configEntity1 = configService.findByType("baiduAIOcrAppId");
			if (configEntity1 != null) ocrAppId = configEntity1.getContent();
			ConfigEntity configEntity2 = configService.findByType("baiduAIOcrAppKey");
			if (configEntity2 != null) ocrAppKey = configEntity2.getContent();
			ConfigEntity configEntity3 = configService.findByType("baiduAIOcrSecretKey");
			if (configEntity3 != null) ocrSecretKey = configEntity3.getContent();
			ConfigEntity configEntity4 = configService.findByType("baiduAIContentCensorAppId");
			if (configEntity4 != null) contentCensorAppId = configEntity4.getContent();
			ConfigEntity configEntity5 = configService.findByType("baiduAIContentCensorAppKey");
			if (configEntity5 != null) contentCensorAppKey = configEntity5.getContent();
			ConfigEntity configEntity6 = configService.findByType("baiduAIContentCensorSecretKey");
			if (configEntity6 != null) contentCensorSecretKey = configEntity6.getContent();
			ConfigEntity configEntity7 = configService.findByType("baiduAISpeechAppId");
			if (configEntity7 != null) speechAppId = configEntity7.getContent();
			ConfigEntity configEntity8 = configService.findByType("baiduAISpeechAppKey");
			if (configEntity8 != null) speechAppKey = configEntity8.getContent();
			ConfigEntity configEntity9 = configService.findByType("baiduAISpeechSecretKey");
			if (configEntity9 != null) speechSecretKey = configEntity9.getContent();
			BaiduAIPojo baiduAIPojo = new BaiduAIPojo(ocrAppId, ocrAppKey, ocrSecretKey, contentCensorAppId,
					contentCensorAppKey, contentCensorSecretKey, speechAppId, speechAppKey,
					speechSecretKey);
			paras[1] = baiduAIPojo;
//		for (int i = 0; i < paras.length; i++){
//			if (paras[i] instanceof BaiduAIPojo){
//				paras[i] = baiduAIPojo;
//				break;
//			}
//		}
		}
		return false;
	}

	@Override
	public void postRun(HookMethod method) {

	}

	@Override
	public boolean onError(HookMethod method) {
		return false;
	}
}
