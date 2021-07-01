package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Member;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.StepEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQLoginLogic;
import me.kuku.yuq.logic.StepLogic;
import me.kuku.yuq.service.MotionService;
import me.kuku.yuq.service.QQLoginService;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@GroupController
@SuppressWarnings("unused")
public class StepController {
    @Inject
    private MotionService motionService;
    @Inject
    @Named("leXin")
    private StepLogic leXinStepLogic;
    @Inject
    @Named("xiaomi")
    private StepLogic xiaomiStepLogic;
    @Inject
    private QQLoginService qqLoginService;
    @Inject
    private QQLoginLogic qqLoginLogic;

    @Before
    public StepEntity before(long qq){
        StepEntity stepEntity = motionService.findByQQ(qq);
        if (stepEntity == null)
            throw FunKt.getMif().at(qq).plus("您还没有绑定账号，无法操作步数！！").toThrowable();
        else return stepEntity;
    }

    @Action("lexin步数 {step}")
    @QMsg(at = true)
    public String leXinStep(StepEntity stepEntity, int step) throws IOException {
        if (stepEntity.getLeXinAccessToken() == null) return "您还没有绑定乐心运动账号，如需绑定请私聊机器人发送<lexin 账号 密码>";
        String result = leXinStepLogic.modifyStepCount(stepEntity, step);
        if (!result.contains("成功")){
            Result<StepEntity> loginResult = leXinStepLogic.loginByPassword(stepEntity.getLeXinPhone(), stepEntity.getLeXinPassword());
            StepEntity loginStepEntity = loginResult.getData();
            if (loginStepEntity == null) {
                stepEntity.setLeXinStatus(false);
                motionService.save(stepEntity);
                return loginResult.getMessage();
            }
            stepEntity.setLeXinCookie(loginStepEntity.getLeXinCookie());
            stepEntity.setLeXinAccessToken(loginStepEntity.getLeXinAccessToken());
            motionService.save(stepEntity);
            result = leXinStepLogic.modifyStepCount(step, stepEntity);
        }
        if (result.contains("成功")){
            QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(stepEntity.getQq());
            if (qqLoginEntity != null){
                qqLoginLogic.motionSign(qqLoginEntity);
            }
        }
        return result;
    }

    @Action("步数任务")
    @QMsg(at = true)
    public String stepTask(StepEntity stepEntity, @PathVar(value = 1, type = PathVar.Type.Integer) Integer step){
        if (step == null) step = 0;
        stepEntity.setStep(step);
        motionService.save(stepEntity);
        return "步数定时任务设置为" + step + "成功";
    }

    @Action("删除步数")
    @QMsg(at = true)
    public String del(long qq){
        motionService.delByQQ(qq);
        return "删除步数成功！！";
    }

    @Action("mi步数 {step}")
    @QMsg(at = true)
    public String xiaomiMotion(StepEntity stepEntity, int step) throws IOException {
        if (stepEntity.getMiLoginToken() == null)
            return "您还没绑定小米账号，如需绑定请私聊机器人发送<mi 账号 密码>";
        String loginToken = stepEntity.getMiLoginToken();
        String result = xiaomiMotionLogic.changeStep(loginToken, step);
        if (result.contains("登录已失效")){
            Result<String> loginResult = xiaomiMotionLogic.login(stepEntity.getMiPhone(), stepEntity.getMiPassword());
            loginToken = loginResult.getData();
            if (loginToken == null) {
                stepEntity.setMiStatus(false);
                motionService.save(stepEntity);
                return loginResult.getMessage();
            }
            stepEntity.setMiLoginToken(loginToken);
            motionService.save(stepEntity);
            result = xiaomiMotionLogic.changeStep(loginToken, step);
        }
        return result;
    }

    @PrivateController
    public static class BindStepController {
        @Inject
        private MotionService motionService;
        @Inject
        private LeXinMotionLogic leXinMotionLogic;
        @Inject
        private XiaomiMotionLogic xiaomiMotionLogic;

        @Action("lexin {phone} {password}")
        public String bindLeXin(String phone, String password, Contact qq) throws IOException {
            String md5Pass = MD5Utils.toMD5(password);
            Result<StepEntity> result = leXinMotionLogic.loginByPassword(phone, md5Pass);
            if (result.getCode() == 200){
                Long group = null;
                if (qq instanceof Member){
                    group = ((Member) qq).getGroup().getId();
                }
                StepEntity stepEntity = result.getData();
                StepEntity newStepEntity = motionService.findByQQ(qq.getId());
                if (newStepEntity == null) newStepEntity = new StepEntity(qq.getId(), group);
                newStepEntity.setLeXinPhone(stepEntity.getLeXinPhone());
                newStepEntity.setLeXinPassword(md5Pass);
                newStepEntity.setLeXinAccessToken(stepEntity.getLeXinAccessToken());
                newStepEntity.setLeXinUserId(stepEntity.getLeXinUserId());
                newStepEntity.setLeXinCookie(stepEntity.getLeXinCookie());
                newStepEntity.setLeXinStatus(true);
                motionService.save(newStepEntity);
                return "绑定乐心运动成功！！";
            }else return result.getMessage();
        }

        @Action("mi {phone} {password}")
        public String bindXiaomiMotion(String phone, String password, Contact qq) {
            Result<String> loginResult = xiaomiMotionLogic.login(phone, password);
            if (loginResult.getCode() == 200){
                Long group = null;
                if (qq instanceof Member){
                    group = ((Member) qq).getGroup().getId();
                }
                String loginToken = loginResult.getData();
                StepEntity stepEntity = motionService.findByQQ(qq.getId());
                if (stepEntity == null) stepEntity = new StepEntity(qq.getId(), group);
                stepEntity.setMiPhone(phone);
                stepEntity.setMiPassword(password);
                stepEntity.setMiLoginToken(loginToken);
                stepEntity.setMiStatus(true);
                motionService.save(stepEntity);
                return "绑定或者更新小米运动信息成功！！";
            }else return "账号或密码错误，请重新绑定！！";
        }
    }

}
