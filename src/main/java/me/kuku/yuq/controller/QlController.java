package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import me.kuku.pojo.Result;
import me.kuku.utils.MyUtils;
import me.kuku.yuq.entity.QlEntity;
import me.kuku.yuq.entity.QlService;
import me.kuku.yuq.logic.JdLogic;
import me.kuku.yuq.pojo.JdQrcode;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@PrivateController
public class QlController extends QQController {

    @Inject
    private QlService qlService;
    @Inject
    private JdLogic jdLogic;
    @Inject
    private JobManager jobManager;
    @Config("YuQ.Mirai.bot.master")
    private String master;
    @Inject
    private MessageItemFactory mif;

    @Before(except = "bindJd")
    public void before(long qq){
        if (!String.valueOf(qq).equals(master))
            throw mif.at(qq).plus("您不为机器人主人，无法执行！").toThrowable();
    }

    @Action("加ql")
    public String add(ContextSession session){
        reply("请在青龙控制面板->系统设置->应用设置 添加应用，权限应拥有环境变量！");
        reply("请输入您的青龙控制面板链接，例如：https://www.baidu.com，最后面不需要有/");
        String url = Message.Companion.firstString(session.waitNextMessage());
        reply("请输入clientId");
        String clientId = Message.Companion.firstString(session.waitNextMessage());
        reply("clientSecret");
        String clientSecret = Message.Companion.firstString(session.waitNextMessage());
        QlEntity qlEntity = QlEntity.Companion.getInstance(url, clientId, clientSecret);
        qlService.save(qlEntity);
        return "添加ql成功！";
    }

    @Action("查ql")
    public String query(){
        StringBuilder sb = new StringBuilder();
        for (QlEntity qlEntity: qlService.findAll()) {
            sb.append(qlEntity.getId()).append("、").append(qlEntity.getUrl()).append("\n");
        }
        if ("".equals(sb.toString())) return "没有绑定任何ql！";
        return MyUtils.removeLastLine(sb);
    }

    @Action("删ql {id}")
    public String del(Integer id){
        qlService.delete(id);
        return "删除成功！";
    }

    @Action("jd")
    public void bindJd(Contact qq) throws IOException {
        List<QlEntity> list = qlService.findAll();
        if (list.isEmpty()) {
            reply("机器人没有绑定青龙面板信息，请联系主人进行绑定！");
            return;
        }
        JdQrcode qrcode = jdLogic.qrcode();
        reply(mif.imageByByteArray(qrcode.getQqLoginQrcode().getBytes()).plus("请使用qq扫码登录京东！"));
        jobManager.registerTimer(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                    Result<String> result = jdLogic.cookie(qrcode);
                    Integer code = result.getCode();
                    if (code == 200){
                        QlEntity qlEntity = list.get(new Random().nextInt(list.size()));
                        Result<?> cc = jdLogic.qlCookie(qlEntity, result.getData());
                        if (cc.isSuccess()) qq.sendMessage("添加至青龙面板成功！"); else qq.sendMessage(cc.getMessage());
                        break;
                    }else if (code == 500){
                        qq.sendMessage(result.getMessage());
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    qq.sendMessage("出现异常了，请重试！异常信息为：" + e.getMessage());
                    break;
                }
            }
        }, 0);
    }



}
