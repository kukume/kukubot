package me.kuku.yuq.controller.motion;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import me.kuku.yuq.entity.MotionEntity;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.LeXinMotionLogic;
import me.kuku.yuq.logic.QQLoginLogic;
import me.kuku.yuq.logic.XiaomiMotionLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.MotionService;
import me.kuku.yuq.service.QQLoginService;

import javax.inject.Inject;
import java.io.IOException;

@GroupController
public class MotionController {
    @Inject
    private MotionService motionService;
    @Inject
    private LeXinMotionLogic leXinMotionLogic;
    @Inject
    private XiaomiMotionLogic xiaomiMotionLogic;
    @Inject
    private QQLoginService qqLoginService;
    @Inject
    private QQLoginLogic qqLoginLogic;

    @Before
    public MotionEntity before(long qq){
        MotionEntity motionEntity = motionService.findByQQ(qq);
        if (motionEntity == null)
            throw FunKt.getMif().at(qq).plus("您还没有绑定账号，无法操作步数！！").toThrowable();
        else return motionEntity;
    }

    @Action("lexin步数 {step}")
    @QMsg(at = true)
    public String leXinStep(MotionEntity motionEntity, int step) throws IOException {
        if (motionEntity.getLeXinAccessToken() == null) return "您还没有绑定乐心运动账号，如需绑定请私聊机器人发送<lexin 账号 密码>";
        String result = leXinMotionLogic.modifyStepCount(step, motionEntity);
        if (!result.contains("成功")){
            Result<MotionEntity> loginResult = leXinMotionLogic.loginByPassword(motionEntity.getLeXinPhone(), motionEntity.getLeXinPassword());
            MotionEntity loginMotionEntity = loginResult.getData();
            if (loginMotionEntity == null) return loginResult.getMessage();
            motionEntity.setLeXinCookie(loginMotionEntity.getLeXinCookie());
            motionEntity.setLeXinAccessToken(loginMotionEntity.getLeXinAccessToken());
            motionService.save(motionEntity);
            result = leXinMotionLogic.modifyStepCount(step, motionEntity);
        }
        if (result.contains("成功")){
            QQLoginEntity qqLoginEntity = qqLoginService.findByQQ(motionEntity.getQq());
            if (qqLoginEntity != null){
                qqLoginLogic.motionSign(qqLoginEntity);
            }
        }
        return result;
    }

    @Action("lexin绑定手环")
    @QMsg(at = true)
    public String bindBand(MotionEntity motionEntity) throws IOException {
        return leXinMotionLogic.bindBand(motionEntity);
    }

    @Action("步数任务")
    @QMsg(at = true)
    public String stepTask(MotionEntity motionEntity, @PathVar(value = 1, type = PathVar.Type.Integer) Integer step){
        if (step == null) step = 0;
        motionEntity.setStep(step);
        motionService.save(motionEntity);
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
    public String xiaomiMotion(MotionEntity motionEntity, int step) throws IOException {
        if (motionEntity.getMiLoginToken() == null)
            return "您还没绑定小米账号，如需绑定请私聊机器人发送<mi 账号 密码>";
        String loginToken = motionEntity.getMiLoginToken();
        String result = xiaomiMotionLogic.changeStep(loginToken, step);
        if (result.contains("登录已失效")){
            Result<String> loginResult = xiaomiMotionLogic.login(motionEntity.getMiPhone(), motionEntity.getMiPassword());
            loginToken = loginResult.getData();
            if (loginToken == null) return loginResult.getMessage();
            motionEntity.setMiLoginToken(loginToken);
            motionService.save(motionEntity);
            result = xiaomiMotionLogic.changeStep(loginToken, step);
        }
        return result;
    }
}
