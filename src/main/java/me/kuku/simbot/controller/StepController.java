package me.kuku.simbot.controller;

import love.forte.simbot.annotation.*;
import me.kuku.pojo.Result;
import me.kuku.simbot.annotation.RegexFilter;
import me.kuku.simbot.entity.QqEntity;
import me.kuku.simbot.entity.StepEntity;
import me.kuku.simbot.entity.StepService;
import me.kuku.simbot.logic.StepLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@OnGroup
@ListenGroup("step")
public class StepController {

	@Autowired
	private StepService stepService;
	@Autowired
	@Qualifier("leXin")
	private StepLogic leXinStepLogic;
	@Autowired
	@Qualifier("xiaomi")
	private StepLogic xiaomiStepLogic;

	@OnPrivate
	@RegexFilter("lexin {{phone}} {{password}}")
	@ListenGroup(value = "", append = false)
	public String login(@FilterValue("phone") String phone, @FilterValue("password") String password,
	                    @ContextValue("qq") QqEntity qqEntity) throws IOException {
		StepEntity stepEntity = stepService.findByQqEntity(qqEntity);
		if (stepEntity == null) stepEntity = StepEntity.Companion.getInstance(qqEntity);
		Result<StepEntity> result = leXinStepLogic.login(phone, password);
		if (result.isSuccess()) {
			StepEntity newStepEntity = result.getData();
			stepEntity.setLeXinPhone(phone);
			stepEntity.setLeXinPassword(password);
			stepEntity.setLeXinAccessToken(newStepEntity.getLeXinAccessToken());
			stepEntity.setLeXinCookie(newStepEntity.getLeXinCookie());
			stepEntity.setLeXinUserid(newStepEntity.getLeXinUserid());
			stepService.save(stepEntity);
			return "绑定乐心运动成功！";
		}else return "绑定乐心运动失败，失败原因：" + result.getMessage();
	}

	@OnPrivate
	@RegexFilter("mi {{phone}} {{password}}")
	@ListenGroup(value = "", append = false)
	public String miLogin(@FilterValue("phone") String phone, @FilterValue("password") String password,
	                      @ContextValue("qq") QqEntity qqEntity) throws IOException {
		StepEntity stepEntity = stepService.findByQqEntity(qqEntity);
		if (stepEntity == null) stepEntity = StepEntity.Companion.getInstance(qqEntity);
		Result<StepEntity> result = xiaomiStepLogic.login(phone, password);
		if (result.isSuccess()){
			stepEntity.setMiPhone(phone);
			stepEntity.setMiPassword(password);
			stepEntity.setMiLoginToken(result.getData().getMiLoginToken());
			stepService.save(stepEntity);
			return "绑定小米运动成功！";
		}else return "绑定小米运动失败，失败原因：" + result.getMessage();
	}

	@RegexFilter("lexin步数{{step}}")
	public String leXinStep(@ContextValue("stepEntity") StepEntity stepEntity,
	                        @FilterValue("step") Integer step) throws IOException {
		Result<String> result = leXinStepLogic.modifyStepCount(stepEntity, step);
		if (result.isSuccess()) return "修改步数成功！";
		else return result.getMessage();
	}

	@RegexFilter("mi步数{{step}}")
	public String miStep(@ContextValue("stepEntity") StepEntity stepEntity,
	                     @FilterValue("step") Integer step) throws IOException {
		Result<String> result = xiaomiStepLogic.modifyStepCount(stepEntity, step);
		if (result.isSuccess()) return "修改步数成功！";
		else return result.getMessage();
	}

	@RegexFilter("步数{{step}}")
	public String cronStep(@ContextValue("stepEntity") StepEntity stepEntity,
	                       @FilterValue("step") Integer step){
		stepEntity.setStep(step);
		stepService.save(stepEntity);
		return "设置步数定时任务成功！";
	}


}
