package me.kuku.yuq.controller.motion;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Member;
import me.kuku.yuq.entity.MotionEntity;
import me.kuku.yuq.logic.LeXinMotionLogic;
import me.kuku.yuq.logic.XiaomiMotionLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.MotionService;
import me.kuku.yuq.utils.MD5Utils;

import javax.inject.Inject;
import java.io.IOException;

@PrivateController
@SuppressWarnings("unused")
public class BindStepController {
    @Inject
    private MotionService motionService;
    @Inject
    private LeXinMotionLogic leXinMotionLogic;
    @Inject
    private XiaomiMotionLogic xiaomiMotionLogic;

    @Action("lexin {phone} {password}")
    public String bindLeXin(String phone, String password, Contact qq) throws IOException {
        String md5Pass = MD5Utils.toMD5(password);
        Result<MotionEntity> result = leXinMotionLogic.loginByPassword(phone, md5Pass);
        if (result.getCode() == 200){
            Long group = null;
            if (qq instanceof Member){
                group = ((Member) qq).getGroup().getId();
            }
            MotionEntity motionEntity = result.getData();
            MotionEntity newMotionEntity = motionService.findByQQ(qq.getId());
            if (newMotionEntity == null) newMotionEntity = new MotionEntity(qq.getId(), group);
            newMotionEntity.setLeXinPhone(motionEntity.getLeXinPhone());
            newMotionEntity.setLeXinPassword(md5Pass);
            newMotionEntity.setLeXinAccessToken(motionEntity.getLeXinAccessToken());
            newMotionEntity.setLeXinUserId(motionEntity.getLeXinUserId());
            newMotionEntity.setLeXinCookie(motionEntity.getLeXinCookie());
            newMotionEntity.setLeXinStatus(true);
            motionService.save(newMotionEntity);
            return "绑定乐心运动成功！！";
        }else return result.getMessage();
    }

    @Action("mi {phone} {password}")
    public String bindXiaomiMotion(String phone, String password, Contact qq) throws IOException {
        Result<String> loginResult = xiaomiMotionLogic.login(phone, password);
        if (loginResult.getCode() == 200){
            Long group = null;
            if (qq instanceof Member){
                group = ((Member) qq).getGroup().getId();
            }
            String loginToken = loginResult.getData();
            MotionEntity motionEntity = motionService.findByQQ(qq.getId());
            if (motionEntity == null) motionEntity = new MotionEntity(qq.getId(), group);
            motionEntity.setMiPhone(phone);
            motionEntity.setMiPassword(password);
            motionEntity.setMiLoginToken(loginToken);
            motionEntity.setMiStatus(true);
            motionService.save(motionEntity);
            return "绑定或者更新小米运动信息成功！！";
        }else return "账号或密码错误，请重新绑定！！";
    }
}
