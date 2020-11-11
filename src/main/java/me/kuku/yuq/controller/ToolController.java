package me.kuku.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Image;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import com.icecreamqaq.yuq.message.XmlEx;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.entity.GroupEntity;
import me.kuku.yuq.logic.QQAILogic;
import me.kuku.yuq.logic.ToolLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.service.GroupService;
import me.kuku.yuq.service.MessageService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.action.Nudge;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings("unused")
@GroupController
public class ToolController {
    @Inject
    private ToolLogic toolLogic;
    @Inject
    private GroupService groupService;
    @Inject
    private QQAILogic qqAiLogic;
    @Inject
    private ConfigService configService;
    @Inject
    private MessageService messageService;
    @Config("YuQ.Mirai.protocol")
    private String protocol;

    @QMsg(at = true)
    @Action("百度 {content}")
    public String teachYouBaidu(String content) throws IOException {
        return toolLogic.teachYou(content, "baidu");
    }

    @QMsg(at = true)
    @Action("谷歌 {content}")
    public String teachYouGoogle(String content) throws IOException {
        return toolLogic.teachYou(content, "google");
    }

    @QMsg(at = true)
    @Action("bing {content}")
    public String teachYouBing(String content) throws IOException {
        return toolLogic.teachYou(content, "bing");
    }

    @QMsg(at = true)
    @Action("搜狗 {content}")
    public String teachYouSouGou(String content) throws IOException {
        return toolLogic.teachYou(content, "sougou");
    }

    @QMsg(at = true, atNewLine = true)
    @Action("舔狗日记")
    public String dogLicking() throws IOException {
        return toolLogic.dogLicking();
    }

    @QMsg(at = true, atNewLine = true)
    @Action("百科 {params}")
    public String baiKe(String params) throws IOException {
        return toolLogic.baiKe(params);
    }

    @QMsg(at = true, atNewLine = true)
    @Action("嘴臭")
    @Synonym({"祖安语录"})
    public String mouthOdor() throws IOException {
        return toolLogic.mouthOdor();
    }

    @QMsg(at = true)
    @Action("毒鸡汤")
    public String poisonousChickenSoup() throws IOException {
        return toolLogic.poisonousChickenSoup();
    }

    @QMsg(at = true)
    @Action("名言")
    public String saying() throws IOException {
        return toolLogic.saying();
    }

    @QMsg(at = true)
    @Action("一言")
    public String hiToKoTo() throws IOException {
        return toolLogic.hiToKoTo().get("text");
    }

    @Action("缩短/{params}")
    @QMsg(at = true)
    public String shortUrl(String params){
        return BotUtils.shortUrl(params);
    }

    @Action("ip/{params}")
    @QMsg(at = true)
    public String queryIp(String params) throws IOException {
        return toolLogic.queryIp(params);
    }

    @Action("whois/{params}")
    @QMsg(at = true, atNewLine = true)
    public String queryWhois(String params) throws IOException {
        return toolLogic.queryWhois(params);
    }

    @Action("icp/{params}")
    @QMsg(at = true, atNewLine = true)
    public String queryIcp(String params) throws IOException {
        return toolLogic.queryIcp(params);
    }

    @Action("知乎日报")
    @QMsg(at = true, atNewLine = true)
    public String zhiHuDaily() throws IOException {
        return toolLogic.zhiHuDaily();
    }

    @QMsg(at = true, atNewLine = true)
    @Action("测吉凶")
    public String qqGodLock(long qq) throws IOException {
        return toolLogic.qqGodLock(qq);
    }

    @QMsg(at = true, atNewLine = true)
    @Action("拼音/{params}")
    public String convertPinYin(String params) throws IOException {
        return toolLogic.convertPinYin(params);
    }

    @QMsg(at = true, atNewLine = true)
    @Action("笑话")
    public String jokes() throws IOException {
        return toolLogic.jokes();
    }

    @QMsg(at = true, atNewLine = true)
    @Action("垃圾/{params}")
    public String rubbish(String params) throws IOException {
        return toolLogic.rubbish(params);
    }

    @Action("解析/{url}")
    @QMsg(at = true, atNewLine = true)
    public String parseVideo(String url) throws IOException {
        return toolLogic.parseVideo(url);
    }

    @Action("还原/{url}")
    @QMsg(at = true)
    public String restoreShortUrl(String url) throws IOException {
        return toolLogic.restoreShortUrl(url);
    }

    @Action("ping/{domain}")
    @QMsg(at = true, atNewLine = true)
    public String ping(String domain) throws IOException {
        return toolLogic.ping(domain);
    }

    @Action("搜 {question}")
    @QMsg(at = true)
    public String search(String question) throws IOException {
        return toolLogic.searchQuestion(question);
    }

    @Action("色图")
    public Message colorPic(long group, long qq) throws IOException {
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null || groupEntity.getColorPic() == null || !groupEntity.getColorPic())
            return FunKt.getMif().at(qq).plus("该功能已关闭！！");
        String type = groupEntity.getColorPicType();
        switch (type){
            case "danbooru":
                byte[] bytes = OkHttpUtils.getBytes("https://api.kuku.me/danbooru");
                return FunKt.getMif().imageByInputStream(new ByteArrayInputStream(bytes)).toMessage();
            case "lolicon":
            case "loliconR18":
                ConfigEntity configEntity = configService.findByType("loLiCon");
                if (configEntity == null) return FunKt.getMif().at(qq).plus("您还没有配置lolicon的apiKey，无法获取色图！！");
                String apiKey = configEntity.getContent();
                Result<Map<String, String>> result = toolLogic.colorPicByLoLiCon(apiKey, type.equals("loliconR18"));
                Map<String, String> map = result.getData();
                if (map == null) return FunKt.getMif().at(qq).plus(result.getMessage());
                byte[] by = toolLogic.piXivPicProxy(map.get("url"));
                return FunKt.getMif().imageByInputStream(new ByteArrayInputStream(by)).toMessage();
            default: return Message.Companion.toMessage("色图类型不匹配！！");
        }
    }

    @Action("qr/{content}")
    @QMsg(at = true, atNewLine = true)
    public Message creatQrCode(String content) throws IOException {
        byte[] bytes = toolLogic.creatQr(content);
        return FunKt.getMif().imageByInputStream(new ByteArrayInputStream(bytes)).toMessage();
    }

    @Action("看美女")
    public Image girl() throws IOException {
        return FunKt.getMif().imageByUrl(toolLogic.girlImage());
    }

    @QMsg(at = true)
    @Action("蓝奏 {url}")
    public String lanZou(String url, @PathVar(2) String pwd) throws UnsupportedEncodingException {
        String resultUrl;
        if (pwd == null)
            resultUrl = "https://v1.alapi.cn/api/lanzou?url=" + URLEncoder.encode(url, "utf-8");
        else resultUrl = "https://v1.alapi.cn/api/lanzou?url=" + URLEncoder.encode(url, "utf-8") + "&pwd=$pwd";
        return BotUtils.shortUrl(resultUrl);
    }

    @Action("lol周免")
    @QMsg(at = true, atNewLine = true)
    public String lolFree() throws IOException {
        return toolLogic.lolFree();
    }

    @Action("缩写/{content}")
    @QMsg(at = true, atNewLine = true)
    public String abbreviation(String content) throws IOException {
        return toolLogic.abbreviation(content);
    }

    @Action("几点了")
    public Image time() throws IOException {
        return FunKt.getMif().imageByInputStream(new ByteArrayInputStream(toolLogic.queryTime()));
    }

    @Action("网抑")
    public XmlEx wy(){
        return FunKt.getMif().xmlEx(1, "<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"1\" templateID=\"-1\" action=\"app\" actionData=\"com.netease.cloudmusic\" brief=\"点击启动网抑\" sourceMsgId=\"0\" url=\"https://www.kuku.me/archives/6/\" flag=\"2\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"12\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://imgurl.cloudimg.cc/2020/07/26/2a7410726090854.jpg\" w=\"0\" h=\"0\" /><title>启动网抑音乐</title></item><source name=\"今天你网抑了吗\" icon=\"\" action=\"\" appid=\"0\" /></msg>");
    }

    @QMsg(at = true)
    @Action("网抑云")
    public String wyy() throws IOException {
        return toolLogic.music163cloud();
    }

    @Action("\\^BV.*\\")
    @Synonym({"\\^bv.*\\"})
    @QMsg(at = true)
    public Message bvToAv(Message message) throws IOException {
        String bv = message.getBody().get(0).toPath();
        Result<Map<String, String>> result = toolLogic.bvToAv(bv);
        if (result.getCode() == 200){
            Map<String, String> map = result.getData();
            MessageItemFactory mif = FunKt.getMif();
            return mif.imageByUrl(map.get("pic")).plus(
                    "标题：" + map.get("title") + "\n" +
                            "描述：" + map.get("desc") +
                            "链接：" + map.get("url")
            );
        }else return Message.Companion.toMessage(result.getMessage());
    }

    @Action("知乎热榜")
    @QMsg(at = true, atNewLine = true)
    public String zhiHuHot() throws IOException {
        List<Map<String, String>> list = toolLogic.zhiHuHot();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++){
            Map<String, String> map = list.get(i);
            sb.append(i + 1).append("、").append(map.get("title")).append("\n");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Action("loc")
    @QMsg(at = true, atNewLine = true)
    public String loc() throws IOException {
        List<Map<String, String>> list = toolLogic.hostLocPost();
        StringBuilder sb = new StringBuilder();
        list.forEach(map ->
                sb.append(map.get("title")).append("-").append(map.get("name")).append("-")
                .append(map.get("time")).append("\n").append("------------").append("\n")
        );
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Action("分词")
    @QMsg(at = true, atNewLine = true)
    public String wordSegmentation(long qq, ContextSession session, Group group) throws IOException {
        group.sendMessage(FunKt.getMif().at(qq).plus("请输入需要中文分词的内容！！"));
        Message nextMessage = session.waitNextMessage();
        return toolLogic.wordSegmentation(Message.Companion.firstString(nextMessage));
    }

    @Action("acg")
    public Image acgPic() throws IOException {
        return FunKt.getMif().imageByUrl(toolLogic.acgPic());
    }

    @Action("搜图 {img}")
    @QMsg(at = true)
    public Message searchImage(Image img) throws IOException {
        String url = toolLogic.identifyPic(img.getUrl());
        if (url != null) return FunKt.getMif().imageByUrl(img.getUrl()).plus(url);
        else return Message.Companion.toMessage("没有找到这张图片！！！");
    }

    @Action("OCR {img}")
    @Synonym({"ocr {img}"})
    @QMsg(at = true, atNewLine = true)
    public String ocr(Image img) throws IOException {
        return qqAiLogic.generalOCR(img.getUrl());
    }

    @Action("github加速 {url}")
    @QMsg(at = true)
    public String githubQuicken(ContextSession session, long qq, String url){
        return BotUtils.shortUrl(toolLogic.githubQuicken(url));
    }

    @Action("traceroute {domain}")
    @Synonym({"路由追踪 {domain}"})
    public String traceRoute(String domain) throws IOException {
        return toolLogic.traceRoute(domain);
    }

    @Action("查发言数")
    public String queryMessage(Group group){
        Map<Long, Long> map = messageService.findCountQQByGroupAndToday(group.getId());
        StringBuilder sb = new StringBuilder().append("本群今日发言数统计如下：").append("\n");
        for (Map.Entry<Long, Long> entry: map.entrySet()){
            sb.append("@").append(group.get(entry.getKey()).nameCardOrName())
                    .append("（").append(entry.getKey()).append("）").append("：")
                    .append(entry.getValue()).append("条").append("\n");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Action("语音合成 {text}")
    public void voice(String text, Group group, long qq) throws IOException {
        Result<byte[]> result = qqAiLogic.voiceSynthesis(text);
        if (result.getCode() == 200){
            net.mamoe.mirai.contact.Group miraiGroup = Bot.getInstance(FunKt.getYuq().getBotId()).getGroup(group.getId());
            miraiGroup.sendMessage(miraiGroup.uploadVoice(new ByteArrayInputStream(result.getData())));
        }else group.sendMessage(FunKt.getMif().at(qq).plus(result.getMessage()));
    }

    @QMsg(at = true)
    @Action("防红 {url}")
    public String preventRed(String url) throws IOException {
        boolean b = new Random().nextBoolean();
        if (b) return toolLogic.preventQQRed(url);
        else return toolLogic.preventQQWechatRed(url);
    }

    @Action("戳 {qqNo}")
    @QMsg(at = true)
    public String stamp(long qqNo, long group){
        if (!"Android".equals(protocol)) return "戳一戳必须使用Android才能使用！！";
        Bot bot = Bot.getInstance(FunKt.getYuq().getBotId());
        net.mamoe.mirai.contact.Group groupObj = bot.getGroup(group);
        Member member;
        if (qqNo == bot.getId()) member = groupObj.getBotAsMember();
        else member = groupObj.getMembers().get(qqNo);
        boolean b = Nudge.Companion.sendNudge(groupObj, member.nudge());
        if (b) return "戳成功！！";
        else return "戳失败，对方已关闭戳一戳！！";
    }
}
