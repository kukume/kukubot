package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PrivateController;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.QqEntity;
import me.kuku.yuq.entity.StepEntity;
import me.kuku.yuq.entity.StepService;
import me.kuku.yuq.logic.StepLogic;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@GroupController
@PrivateController
public class StepController {

	@Inject
	private StepService stepService;
	@Inject
	@Named("leXin")
	private StepLogic leXinStepLogic;
	@Inject
	@Named("xiaomi")
	private StepLogic xiaomiStepLogic;

	@Before(except = {"login", "miLogin"})
	public StepEntity before(long qq, QqEntity qqEntity){
		StepEntity stepEntity = stepService.findByQqEntity(qqEntity);
		if (stepEntity == null)
			throw FunKt.getMif().at(qq).plus("您没有绑定step，请私聊机器人绑定！").toThrowable();
		else return stepEntity;
	}

	@Action("lexin {phone} {password}")
	public String login(String phone, String password, QqEntity qqEntity) throws IOException {
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

	@Action("mi {phone} {password}")
	public String miLogin(String phone, String password, QqEntity qqEntity) throws IOException {
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

	@Action("lexin步数 {step}")
	public String leXinStep(StepEntity stepEntity, Integer step) throws IOException {
		Result<String> result = leXinStepLogic.modifyStepCount(stepEntity, step);
		if (result.isSuccess()) return "修改步数成功！";
		else return result.getMessage();
	}

	@Action("mi步数{{step}}")
	public String miStep(StepEntity stepEntity, Integer step) throws IOException {
		Result<String> result = xiaomiStepLogic.modifyStepCount(stepEntity, step);
		if (result.isSuccess()) return "修改步数成功！";
		else return result.getMessage();
	}

	@Action("步数{{step}}")
	public String cronStep(StepEntity stepEntity, Integer step){
		stepEntity.setStep(step);
		stepService.save(stepEntity);
		return "设置步数定时任务成功！";
	}

}
