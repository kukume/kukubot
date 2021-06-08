package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.MotionEntity;
import me.kuku.yuq.pojo.Result;

import java.io.IOException;
import java.io.InputStream;

@AutoBind
public interface LeXinMotionLogic {
    InputStream getCaptchaImage(String phone) throws IOException;
    Result<String> getCaptchaCode(String phone, String captchaImageCode) throws IOException;
    Result<MotionEntity> loginByPassword(String phone, String password) throws IOException;
    Result<MotionEntity> loginByPhoneCaptcha(String phone, String captchaPhoneCode) throws IOException;
    String modifyStepCount(int step, MotionEntity motionEntity) throws IOException;
    String bindBand(MotionEntity motionEntity) throws IOException;
}
