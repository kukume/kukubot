package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.pojo.UA;
import me.kuku.utils.MyUtils;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.QqUtils;
import me.kuku.yuq.entity.QqLoginEntity;
import me.kuku.yuq.entity.QqVideoEntity;
import me.kuku.yuq.entity.QqVideoService;
import me.kuku.yuq.logic.QqLoginLogic;
import me.kuku.yuq.pojo.GroupMember;
import me.kuku.yuq.utils.QqSuperLoginUtils;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class QQLoginLogicImpl implements QqLoginLogic {

    @Resource
    private QqVideoService qqVideoService;

    @Override
    public Result<Map<String, String>> groupUploadImage(QqLoginEntity QqLoginEntity, String url) throws IOException {
        byte[] bytes = OkHttpUtils.getBytes(url);
        MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("bkn", QqLoginEntity.getGtk())
                .addFormDataPart("pic_up", Base64.getEncoder().encodeToString(bytes)).build();
        JSONObject jsonObject = OkHttpUtils.postJson("https://qun.qq.com/cgi-bin/qiandao/upload/pic", body,
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        if (jsonObject.getInteger("retcode").equals(0)){
            JSONObject dataJsonObject = jsonObject.getJSONObject("data");
            Map<String, String> map = new HashMap<>();
            map.put("picId", dataJsonObject.getString("pic_id"));
            map.put("picUrl", dataJsonObject.getString("pic_url"));
            return Result.success(map);
        }else return Result.failure("上传图片失败，" + jsonObject.getString("msg"), null);
    }

    @Override
    public String vipSign(QqLoginEntity QqLoginEntity) throws IOException {
        StringBuilder sb = new StringBuilder();
        String gtk2 = QqLoginEntity.getGtk2();
        Headers cookie = OkHttpUtils.addCookie(QqLoginEntity.getCookie());
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=79968&format=json&g_tk=%s&cachetime=%d", gtk2, System.currentTimeMillis()), cookie);
        switch (jsonObject.getInteger("ret")){
            case 0: sb.append("会员面板签到成功！！\n"); break;
            case 10601: sb.append("会员面板今天已经签到！\n"); break;
            case 10002: sb.append("会员面板签到失败！请更新QQ！\n"); break;
            case 20101: sb.append("会员面板签到失败，不是QQ会员！\n"); break;
        }
        jsonObject = OkHttpUtils.getJson(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=403490&rand=0.27489888%s&g_tk=%s&format=json", System.currentTimeMillis(), gtk2), cookie);
        switch (jsonObject.getInteger("ret")){
            case 0: sb.append("会员电脑端签到成功！！\n"); break;
            case 10601: sb.append("会员电脑端今天已经签到！\n"); break;
            case 10002: sb.append("会员电脑端签到失败！请更新QQ！\n"); break;
            case 20101: sb.append("会员电脑端签到失败，不是QQ会员！\n"); break;
        }
        jsonObject = OkHttpUtils.getJson("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=23074&format=json&g_tk=" + gtk2, cookie);
        switch (jsonObject.getInteger("ret")){
            case 0: sb.append("会员积分手机端签到成功！！\n"); break;
            case 10601: sb.append("会员积分手机端今天已经签到！\n"); break;
            case 10002: sb.append("会员积分手机端签到失败！请更新QQ！\n"); break;
        }
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=27754&_=%d", gtk2, System.currentTimeMillis()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=27754&_=%d", gtk2, System.currentTimeMillis()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=22894&_c=page&_=%d", gtk2, System.currentTimeMillis()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi4.vip.qq.com/ams3.0.php?g_tk=%s&actid=239371&_c=page&format=json&_=%d", gtk2, System.currentTimeMillis()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=22887&_c=page&format=json&_=%d", gtk2, System.currentTimeMillis()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=202041&_c=page&format=json&_=%d", gtk2, System.currentTimeMillis()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=202049&_c=page&format=json&_=%d", gtk2, System.currentTimeMillis()), cookie).close();
        return sb.toString();
    }

    @Override
    public String queryVip(QqLoginEntity QqLoginEntity) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://api.unipay.qq.com/v1/r/1450000172/wechat_query?cmd=7&pf=vip_m-50000-html5&pfkey=pfkey&session_id=uin&expire_month=0&session_type=skey&openid=%s&openkey=%s&format=jsonp__myserviceIcons", QqLoginEntity.getQqEntity().getQq(), QqLoginEntity.getSKey()),
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        if (jsonObject.getInteger("ret").equals(0)){
            JSONArray jsonArray = jsonObject.getJSONArray("service");
            StringBuilder sb = new StringBuilder(String.format("一共为您查询到%d项业务", jsonArray.size()));
            jsonArray.forEach(obj -> {
                JSONObject vipJsonObject = (JSONObject) obj;
                String name;
                if (vipJsonObject.containsKey("year_service_name")) name = vipJsonObject.getString("year_service_name");
                else if (vipJsonObject.containsKey("upgrade_service_name")) name = vipJsonObject.getString("upgrade_service_name");
                else name = vipJsonObject.getString("service_name");
                sb.append("业务名称：").append(name).append("\n")
                        .append("开通日期：").append(vipJsonObject.getString("start_time")).append("\n")
                        .append("到期日期：").append(vipJsonObject.getString("end_time")).append("\n")
                        .append("------------").append("\n");
            });
            return sb.deleteCharAt(sb.length() - 1).toString();
        }else return "业务查询失败，请更新QQ！";
    }

    @Override
    public String yellowSign(QqLoginEntity QqLoginEntity) throws IOException {
        StringBuilder sb = new StringBuilder();
        String gtkP = QqLoginEntity.getGtkP();
        Map<String, String> map = new HashMap<>();
        map.put("uin", String.valueOf(QqLoginEntity.getQqEntity().getQq()));
        map.put("format", "json");
        JSONObject jsonObject = OkHttpUtils.postJson(String.format("https://vip.qzone.qq.com/fcg-bin/v2/fcg_mobile_vip_site_checkin?t=0.89457%d&g_tk=%s&qzonetoken=423659183", System.currentTimeMillis(), gtkP),
                map, OkHttpUtils.addCookie(QqLoginEntity.getCookieWithPs()));
        switch (jsonObject.getInteger("code")){
            case 0: sb.append("黄钻签到成功！"); break;
            case -3000: sb.append("黄钻签到失败！请更新QQ！"); break;
            default: sb.append("黄钻今日已签到！！");
        }
        map.clear();
        map.put("option", "sign");
        map.put("uin", String.valueOf(QqLoginEntity.getQqEntity().getQq()));
        map.put("format", "json");
        jsonObject = OkHttpUtils.postJson(String.format("https://activity.qzone.qq.com/fcg-bin/fcg_huangzuan_daily_signing?t=0.%s906035&g_tk=%s&qzonetoken=-1", System.currentTimeMillis(), gtkP),
                map, OkHttpUtils.addCookie(QqLoginEntity.getCookieWithPs()));
        switch (jsonObject.getInteger("code")){
            case 0: sb.append("黄钻公众号签到成功！"); break;
            case -3000: sb.append("黄钻公众号签到失败！请更新QQ！"); break;
            case -90002: sb.append("抱歉，您不是黄钻用户，签到失败"); break;
            default: sb.append("黄钻今日已签到！");
        }
        return sb.toString();
    }

    @Override
    public String qqVideoSign1(QqLoginEntity QqLoginEntity) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://vip.video.qq.com/fcgi-bin/comm_cgi?name=hierarchical_task_system&cmd=2&_=%s8906", System.currentTimeMillis()),
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ret")){
            case 0: return "腾讯视频会员签到成功";
            case -10006: return "腾讯视频会员签到失败，请更新QQ！";
            case -10019: return "您不是腾讯视频会员，签到失败！";
            default: return "腾讯视频会员签到失败，" + jsonObject.getString("msg");
        }
    }

    @Override
    public String qqVideoSign2(QqLoginEntity QqLoginEntity) throws IOException {
        Response response = OkHttpUtils.get("https://access.video.qq.com/user/auth_login?vappid=11059694&vsecret=fdf61a6be0aad57132bc5cdf78ac30145b6cd2c1470b0cfe&login_flag=1&type=qq&appid=101483052&g_tk=" + QqLoginEntity.getGtk() + "&g_vstk=&g_actk=&callback=jQuery19107079438303985055_1588043611061&_=" + System.currentTimeMillis(),
                OkHttpUtils.addCookie(QqLoginEntity.getCookie() + "video_guid=87f1f5fd3c3ebf5a; video_platform=2; "));
        response.close();
        String cookie = OkHttpUtils.getCookie(response);
        if (cookie.contains("vusession")){
            String html = OkHttpUtils.getStr("https://v.qq.com/x/bu/mobile_checkin",
                    OkHttpUtils.addCookie(QqLoginEntity.getCookie() + cookie + "video_guid=fd42304ceeead2c8; video_platform=2; "));
            if (!html.contains("签到失败")) return "签到成功！！";
            else return "签到失败，请先去腾讯视频app私信\"https://v.qq.com/x/bu/mobile_checkin\"并打开该链接";
        }else return "腾讯视频二次签到失败！请更新QQ！";
    }

    @Override
    public String bigVipSign(QqLoginEntity QqLoginEntity) throws IOException {
        OkHttpUtils.get("https://h5.qzone.qq.com/qzone/visitor?_wv=3&_wwv=1024&_proxy=1", OkHttpUtils.addCookie(QqLoginEntity.getCookie())).close();
        Map<String, String> map = new HashMap<>();
        map.put("outCharset", "utf-8");
        map.put("iAppId", "0");
        map.put("llTime", String.valueOf(System.currentTimeMillis()));
        map.put("format", "json");
        map.put("iActionType", "6");
        map.put("strUid", String.valueOf(QqLoginEntity.getQqEntity().getQq()));
        map.put("uin", String.valueOf(QqLoginEntity.getQqEntity().getQq()));
        map.put("inCharset", "utf-8");
        JSONObject jsonObject1 = OkHttpUtils.postJson("https://h5.qzone.qq.com/webapp/json/QQBigVipTask/CompleteTask?t=0." + System.currentTimeMillis() + "906319&g_tk=" + QqLoginEntity.getGtkP(),
                map, OkHttpUtils.addCookie(QqLoginEntity.getCookieWithSuper()));
        map.clear();
        map.put("appid", "qq_big_vip");
        map.put("op", "CheckIn");
        map.put("uin", String.valueOf(QqLoginEntity.getQqEntity().getQq()));
        map.put("format", "json");
        map.put("inCharset", "utf-8");
        map.put("outCharset", "utf-8");
        JSONObject jsonObject2 = OkHttpUtils.postJson("https://vip.qzone.qq.com/fcg-bin/v2/fcg_vip_task_checkin?t=0" + System.currentTimeMillis() + "082161&g_tk=" + QqLoginEntity.getGtkP(), map,
                OkHttpUtils.addCookie(QqLoginEntity.getCookieWithPs()));
        if (jsonObject1.getInteger("ret") == 0 && jsonObject2.getInteger("code") == 0) return "大会员签到成功！！";
        else if (jsonObject1.getInteger("ret") == -3000 && jsonObject2.getInteger("code") == -3000) return "大会员签到失败！请更新QQ！";
        else return "大会员签到失败！！";
    }

    @Override
    public String weiYunSign(QqLoginEntity QqLoginEntity) throws IOException {
        Result<String> result = QqSuperLoginUtils.weiYunLogin(QqLoginEntity);
        if (result.getCode() == 200){
            String psKey = result.getData();
            String str = OkHttpUtils.getStr("https://h5.weiyun.com/sign_in", OkHttpUtils.addCookie(QqLoginEntity.getCookie(psKey)));
            String json = MyUtils.regex("(?<=window\\.__INITIAL_STATE__=).+?(?=</script>)", str);
            JSONObject jsonObject = JSON.parseObject(json);
            return String.format("微云签到成功，已连续签到%d天，当前金币%d",
                    jsonObject.getJSONObject("index").getInteger("consecutiveSignInCount"), jsonObject.getJSONObject("global").getInteger("totalCoin"));
        }else return "微云签到失败，请更新QQ！";
    }

    @Override
    public String qqMusicSign(QqLoginEntity QqLoginEntity) throws IOException {
        StringBuilder sb = new StringBuilder();
        String url = "https://u.y.qq.com/cgi-bin/musicu.fcg";
        Headers headers = OkHttpUtils.addHeaders(QqLoginEntity.getCookie(), url);
        String gtk = QqLoginEntity.getGtk();
        JSONObject jsonObject = OkHttpUtils.postJson(url,
                OkHttpUtils.addJson("{\"req_0\":{\"module\":\"UserGrow.UserGrowScore\",\"method\":\"receive_score\",\"param\":{\"musicid\":\"" + QqLoginEntity.getQqEntity().getQq() + "\",\"type\":15}},\"comm\":{\"g_tk\":" + gtk + ",\"uin\":" + QqLoginEntity.getQqEntity().getQq() + ",\"format\":\"json\",\"ct\":23,\"cv\":0}}"),
                headers);
        JSONObject reqJsonObject = jsonObject.getJSONObject("req_0");
        if (reqJsonObject == null) return "签到失败";
        jsonObject = reqJsonObject.getJSONObject("data");
        switch (jsonObject.getInteger("retCode")){
            case 0: sb.append(String.format("QQ音乐签到成功！获得积分：%s，签到天数：%s，总积分：%s",
                    jsonObject.getString("todayScore"), jsonObject.getString("totalDays"), jsonObject.getString("totalScore"))); break;
            case 40001: sb.append("QQ音乐今日已签到！"); break;
            case -13004: sb.append("QQ音乐签到失败！请更新QQ！"); break;
            default: sb.append("QQ音乐签到失败！").append(jsonObject.getString("errMsg"));
        }
        jsonObject = OkHttpUtils.postJson(url,
                OkHttpUtils.addJson("{\"comm\":{\"g_tk\":" + gtk + ",\"uin\":" + QqLoginEntity.getQqEntity().getQq() + ",\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"h5\",\"needNewCode\":1,\"ct\":23,\"cv\":0},\"req_0\":{\"module\":\"music.activeCenter.ActiveCenterSignSvr\",\"method\":\"DoSignIn\",\"param\":{}}}"),
                headers);
        jsonObject = jsonObject.getJSONObject("req_0").getJSONObject("data");
        switch (jsonObject.getInteger("retCode")){
            case 0: sb.append(String.format("QQ音乐活动签到成功！已连续签到%s天，累计签到%s天",
                    jsonObject.getJSONObject("signInfo").getString("continuousDays"), jsonObject.getJSONObject("signInfo").getString("totalDays"))); break;
            case 40004: sb.append("QQ音乐活动今日已签到！"); break;
            case -13004: sb.append("QQ音乐签到失败！请更新QQ！"); break;
            default: sb.append("QQ音乐签到失败！").append(jsonObject.getString("errMsg")); break;
        }
        jsonObject = OkHttpUtils.postJson(url,
                OkHttpUtils.addJson("{\"req_0\":{\"module\":\"UserGrow.UserGrowScore\",\"method\":\"receive_score\",\"param\":{\"musicid\":\"" + QqLoginEntity.getQqEntity().getQq() + "\",\"type\":1}},\"comm\":{\"g_tk\":" + gtk + ",\"uin\":" + QqLoginEntity.getQqEntity().getQq() + ",\"format\":\"json\",\"ct\":23,\"cv\":0}}"),
                headers);
        jsonObject = jsonObject.getJSONObject("req_0").getJSONObject("data");
        switch (jsonObject.getInteger("retCode")){
            case 0: sb.append(String.format("QQ音乐分享成功！获得积分：%s天，签到天数：%s天，总积分:%s",
                    jsonObject.getString("todayScore"), jsonObject.getString("totalDays"), jsonObject.getString("totalScore"))); break;
            case 40001: sb.append("QQ音乐今日已分享！"); break;
            case 40002: sb.append("QQ音乐今日分享未完成！"); break;
            case -13004: sb.append("QQ音乐分享失败！请更新QQ！"); break;
            default: sb.append("QQ音乐分享失败！").append(jsonObject.getString("errMsg")); break;
        }
        jsonObject = OkHttpUtils.postJson(url,
                OkHttpUtils.addJson("{\"req_0\":{\"module\":\"Radio.RadioLucky\",\"method\":\"clockIn\",\"param\":{\"platform\":2}},\"comm\":{\"g_tk\":" + QqLoginEntity.getGtkP() + ",\"uin\":" + QqLoginEntity.getQqEntity().getQq() + ",\"format\":\"json\"}}"),
                headers);
        jsonObject = jsonObject.getJSONObject("req_0").getJSONObject("data");
        switch (jsonObject.getInteger("retCode")){
            case 0: sb.append("QQ音乐电台锦鲤打卡成功！积分+").append(jsonObject.getString("score")); break;
            case 40001: sb.append("QQ音乐电台锦鲤已打卡！"); break;
            case -13004: sb.append("QQ音乐电台锦鲤打卡失败！请更新QQ！"); break;
            default: sb.append("QQ音乐电台锦鲤打卡失败！").append(jsonObject.getString("errMsg")); break;
        }
        return sb.toString();
    }

    @Override
    public String gameSign(QqLoginEntity QqLoginEntity) throws IOException {
        String gtk = QqLoginEntity.getGtk();
        StringBuilder sb = new StringBuilder();
        String qq = String.valueOf(QqLoginEntity.getQqEntity().getQq());
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("http://social.minigame.qq.com/cgi-bin/social/welcome_panel_operate?format=json&cmd=2&uin=%s&g_tk=%s", qq, gtk),
                OkHttpUtils.addHeaders(QqLoginEntity.getCookie(), "http://minigame.qq.com/appdir/social/cloudHall/src/index/welcome.html"));
        switch (jsonObject.getInteger("result")){
            case 0: {
                if (jsonObject.getInteger("do_ret") == 11) sb.append("游戏大厅今天已签到！\n");
                else sb.append("游戏大厅签到成功！\n");
                break;
            }
            case 1000005: sb.append("游戏大厅签到失败！请更新QQ！\n"); break;
            default: sb.append("游戏大厅签到失败！").append(jsonObject.getString("resultstr")).append("\n");
        }
        jsonObject = OkHttpUtils.getJson("http://social.minigame.qq.com/cgi-bin/social/CheckInPanel_Operate?Cmd=CheckIn_Operate&g_tk=" + gtk,
                OkHttpUtils.addHeaders(QqLoginEntity.getCookie(), "http://minigame.qq.com/appdir/social/cloudHall/src/index/welcome.html"));
        switch (jsonObject.getInteger("result")){
            case 0: sb.append("游戏大厅2签到成功！！\n"); break;
            case 1000005: sb.append("游戏大厅2签到失败！请更新QQ！\n"); break;
            default: sb.append("游戏大厅签到2失败！").append(jsonObject.getString("resultstr")).append("\n");
        }
        Response response = OkHttpUtils.get("http://info.gamecenter.qq.com/cgi-bin/gc_my_tab_async_fcgi?merge=1&ver=0&st=" + System.currentTimeMillis() + "746&sid=&uin=" + qq + "&number=0&path=489&plat=qq&gamecenter=1&_wv=1031&_proxy=1&gc_version=2&ADTAG=gamecenter&notShowPub=1&param=%7B%220%22%3A%7B%22param%22%3A%7B%22platform%22%3A1%2C%22tt%22%3A1%7D%2C%22module%22%3A%22gc_my_tab%22%2C%22method%22%3A%22sign_in%22%7D%7D&g_tk=" + gtk,
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        if (response.code() == 200){
            jsonObject = OkHttpUtils.getJson(response);
            switch (jsonObject.getInteger("ecode")){
                case 0: {
                    jsonObject = jsonObject.getJSONObject("data").getJSONObject("0");
                    if (jsonObject.getInteger("retCode") == 0)
                        sb.append(String.format("手Q游戏中心签到成功！已连续签到%s天",
                                jsonObject.getJSONObject("retBody").getJSONObject("data").getInteger("cur_continue_sign"))).append("\n");
                    else sb.append("手Q游戏中心签到失败！").append(jsonObject.getJSONObject("retBody").getString("message"));
                    break;
                }
                case -120000: sb.append("手Q游戏中心签到失败！请更新QQ！\n"); break;
                default: sb.append("手Q游戏中心签到失败！\n");
            }
        }else response.close();
        response = OkHttpUtils.get("https://1.game.qq.com/app/sign?start=" + new SimpleDateFormat("yyyy-MM").format(new Date()) +
                "&g_tk=$gtk&_t=0.6780016267291531", OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        String jsonStr = MyUtils.regex("(?<=var sign_index = ).*?(?=;)", OkHttpUtils.getStr(response));
        jsonObject = JSON.parseObject(jsonStr);
        Integer iRet = jsonObject.getInteger("iRet");
        switch (iRet){
            case 0: sb.append("精品页游签到成功！\n"); break;
            case 1: sb.append("精品页游今天已签到！\n"); break;
            default: sb.append("精品页游签到失败！").append(jsonObject.getString("sMsg"));
        }
        Map<String, String> map = new HashMap<>();
        map.put("iActivityId", "11117");
        map.put("iFlowId", "96939");
        map.put("g_tk", gtk);
        map.put("e_code", "0");
        map.put("g_code", "0");
        map.put("sServiceDepartment", "djc");
        map.put("sServiceType", "dj");
        jsonObject = OkHttpUtils.postJson("https://apps.game.qq.com/ams/ame/ame.php?ameVersion=0.3&sServiceType=dj&iActivityId=11117&sServiceDepartment=djc&set_info=djc",
                map, OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        jsonObject = jsonObject.getJSONObject("modRet");
        if (jsonObject == null) sb.append("道聚城签到失败\n");
        else {
            switch (jsonObject.getInteger("ret")){
                case 0: sb.append("道聚城签到成功！\n"); break;
                case 600: sb.append("道聚城今天已签到！\n"); break;
                default: sb.append("道聚城签到失败！").append(jsonObject.getJSONObject("modRet").getString("msg")).append("\n");
            }
        }
        String dnfUrl = "https://apps.game.qq.com/cms/index.php?serviceType=dnf&actId=2&sAction=duv&sModel=Data&retType=json";
        jsonObject = OkHttpUtils.getJson(dnfUrl, OkHttpUtils.addHeaders(QqLoginEntity.getCookie(), dnfUrl));
        iRet = jsonObject.getInteger("iRet");
        if (iRet == 0){
            if (jsonObject.getJSONObject("jData").getInteger("iLotteryRet") == 100002) sb.append("DNF社区积分已领取！");
            else sb.append("DNF社区积分领取成功！");
        }else sb.append("DNF社区积分领取失败！");
        return sb.toString();
    }

    @Override
    public String qPetSign(QqLoginEntity QqLoginEntity) throws IOException {
        Response response = OkHttpUtils.get("https://fight.pet.qq.com/cgi-bin/petpk?cmd=award&op=1&type=0", OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        if (response.code() != 200){
            response.close();
            return "大乐斗礼包领取失败！！";
        }
        JSONObject jsonObject = OkHttpUtils.getJson(response);
        Integer ret = jsonObject.getInteger("ret");
        if (ret == null) return "大乐斗领礼包失败";
        else if (ret == -1 || ret == 0) return "大乐斗" + jsonObject.getString("ContinueLogin") + jsonObject.getString("DailyAward");
        else if (ret == 5) return "大乐斗领礼包失败！请更新QQ！";
        else return "大乐斗领礼包失败！";
    }

    @Override
    public String blueSign(QqLoginEntity QqLoginEntity) throws IOException {
        Result<String> result = QqSuperLoginUtils.blueLogin(QqLoginEntity);
        if (result.getCode() == 200){
            String psKey = result.getData();
            String cookie = QqLoginEntity.getCookie(psKey) + "DomainID=176; ";
            String gtk = QqLoginEntity.getGtk();
            StringBuilder sb = new StringBuilder();
            JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://app.gamevip.qq.com/cgi-bin/gamevip_sign/GameVip_SignIn?format=json&g_tk=%s&_=%s", gtk, System.currentTimeMillis()),
                    OkHttpUtils.addHeaders(cookie, "https://gamevip.qq.com/sign_pop/sign_pop_v2.html"));
            switch (jsonObject.getInteger("result")){
                case 0: sb.append("蓝钻签到成功！当前签到积分").append(jsonObject.getString("SignScore")).append("点\n"); break;
                case 1000005: sb.append("蓝钻签到失败，请更新QQ！！\n"); break;
                default: sb.append("蓝钻签到失败！").append(jsonObject.getString("resultstr"));
            }
            jsonObject = OkHttpUtils.getJson(String.format("https://app.gamevip.qq.com/cgi-bin/gamevip_sign/GameVip_Lottery?format=json&g_tk=%s&_=%s0334", gtk, System.currentTimeMillis()),
                    OkHttpUtils.addHeaders(cookie, "https://gamevip.qq.com/sign_pop/sign_pop_v2.html"));
            switch (jsonObject.getInteger("result")){
                case 0: sb.append("蓝钻抽奖成功！\n"); break;
                case 1000005: sb.append("蓝钻抽奖失败！请更新QQ！\n"); break;
                case 102: sb.append("蓝钻抽奖次数已用完！\n"); break;
                default: sb.append("蓝钻抽奖失败！").append(jsonObject.getString("resultstr")).append("\n");
            }
            jsonObject = OkHttpUtils.getJson("https://app.gamevip.qq.com/cgi-bin/gamevip_m_sign/GameVip_m_SignIn",
                    OkHttpUtils.addHeaders(cookie, "https://gamevip.qq.com/sign_pop/sign_pop_v2.html"));
            switch (jsonObject.getInteger("result")){
                case 0: sb.append("蓝钻手机签到成功！！"); break;
                case 1000005: sb.append("蓝钻手机签到失败！请更新QQ！"); break;
                default: sb.append("蓝钻抽奖失败！").append(jsonObject.getString("resultstr"));
            }
            return sb.toString();
        }else return "蓝钻签到失败，请更新QQ！";
    }

    @Override
    public String sendFlower(QqLoginEntity QqLoginEntity, Long qq, Long group) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("instanceID", "537064459");
        map.put("giftID", "99");
        map.put("channel", "1");
        map.put("goodsId", "flower");
        map.put("count", "3");
        map.put("from", "0");
        map.put("toUin", qq.toString());
        map.put("isCustom", "1");
        map.put("rule", "0");
        map.put("gc", group.toString());
        map.put("_r", "229");
        map.put("version", "Android8.3.6.4590");
        map.put("bkn", QqLoginEntity.getGtk());
        JSONObject jsonObject = OkHttpUtils.postJson("https://pay.qun.qq.com/cgi-bin/group_pay/good_feeds/send_goods", map,
                OkHttpUtils.addHeaders(QqLoginEntity.getCookie(), "https://qun.qq.com/qunpay/gifts/index.html?troopUin=$group&uin=$qq&name=&from=profilecard&_wv=1031&_bid=2204&_wvSb=1&_nav_alpha=0"));
        switch (jsonObject.getInteger("ec")){
            case 0: return "送花成功";
            case 4:
            case 1: return "送花失败，请更新QQ";
            case 20000: return "鲜花不足，充点钱再送把！";
            default: return "送花失败，" + jsonObject.getString("em");
        }
    }

    private Integer getBubbleId(QqLoginEntity QqLoginEntity, String psKey, String name) throws IOException {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 5; i++){
            map.put("page", String.valueOf(i));
            map.put("num", "15");
            JSONObject jsonObject = OkHttpUtils.postJson(String.format("https://zb.vip.qq.com/bubble/cgi/getDiyBubbleList?daid=18&g_tk=%s&p_tk=%s", QqLoginEntity.getGtk(), QqLoginEntity.getPt4Token()),
                    map, OkHttpUtils.addHeaders(QqLoginEntity.getCookie(psKey) + "pt4_token=" + QqLoginEntity.getPt4Token(), null, UA.QQ));
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
            for (Object obj: jsonArray){
                JSONObject bubbleJsonObject = (JSONObject) obj;
                String bubbleName = bubbleJsonObject.getJSONArray("baseInfo").getJSONObject(0).getString("name");
                if (name.equals(bubbleName)){
                    return bubbleJsonObject.getInteger("id");
                }
            }
        }
        return null;
    }

    @Override
    public String diyBubble(QqLoginEntity QqLoginEntity, String text, String name) throws IOException {
        Result<String> result = QqSuperLoginUtils.vipLogin(QqLoginEntity);
        if (result.getCode() == 200){
            Integer id;
            if (name == null){
                String ids = "2551|2514|2516|2493|2494|2464|2465|2428|2427|2426|2351|2319|2320|2321|2232|2239|2240|2276|2275|2274|2273|2272|2271";
                String[] arr = ids.split("\\|");
                id = Integer.parseInt(arr[(int) (Math.random() * arr.length)]);
            }else id = getBubbleId(QqLoginEntity, result.getData(), name);
            if (id != null){
                JSONObject jsonObject = OkHttpUtils.getJsonp("https://g.vip.qq.com/bubble/bubbleSetup?id=" + id + "&platformId=2&uin=" + QqLoginEntity.getQqEntity().getQq() + "&version=8.3.0.4480&diyText=%7B%22diyText%22%3A%22" + text + "%22%7D&format=jsonp&t=" + System.currentTimeMillis() + "&g_tk=" + QqLoginEntity.getGtk() + "&p_tk=" + QqLoginEntity.getPt4Token() + "&callback=jsonp0",
                        OkHttpUtils.addCookie(QqLoginEntity.getCookie(result.getData())));
                switch (jsonObject.getInteger("ret")){
                    case 0: return "更换气泡成功，由于缓存等原因，效果可能会在较长一段时间后生效！";
                    case -100001: return "更换气泡失败，请更新QQ！";
                    case 5002:
                    case 2002: return "您不是睾贵的超级会员用户，更换气泡失败！";
                    default: return "更换气泡失败，" + jsonObject.getString("msg");
                }
            }else return "抱歉，未找到该气泡名字，更换气泡失败！";
        }else return "更换气泡失败！请更新QQ！";
    }

    @Override
    public String vipGrowthAdd(QqLoginEntity QqLoginEntity) throws IOException {
        LocalDate now = LocalDate.now();
        String nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        JSONObject jsonObject = OkHttpUtils.postJson("https://proxy.vac.qq.com/cgi-bin/srfentry.fcgi?ts=" + System.currentTimeMillis() + "&g_tk=" + QqLoginEntity.getGtk(),
                OkHttpUtils.addJson(String.format("{\"13357\":{\"month\":%s,\"pageIndex\":1,\"pageSize\":20,\"sUin\":\"%s\",\"year\":%d}}",
                        now.getMonth().getValue(), QqLoginEntity.getQqEntity().getQq(), now.getYear())),
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        if (jsonObject.getInteger("ecode") == 0){
            JSONArray jsonArray = jsonObject.getJSONObject("13357").getJSONObject("data").getJSONObject("growthRecord").getJSONArray("record");
            StringBuilder sb = new StringBuilder();
            int allAdd = 0;
            for (Object obj: jsonArray){
                JSONObject singleJsonObject = (JSONObject) obj;
                if (!simpleDateFormat.format(new Date(Long.parseLong(singleJsonObject.getString("acttime") + "000"))).equals(nowDate)) break;
                String type;
                switch (singleJsonObject.getInteger("actid")){
                    case 126: type = "手机QQ签到"; break;
                    case 140: type = "节日签到"; break;
                    case -9999: type = "每日成长值"; break;
                    case 169: type = "QQ会员官方账号每日签到"; break;
                    case 664: type = "早起走运"; break;
                    case 662: type = "成长值排名&点赞"; break;
                    case 697:
                    case 703: type = singleJsonObject.getString("actname"); break;
                    case 26: type = "活动赠送"; break;
                    case 136: type = "超级会员每月礼包"; break;
                    case 659: type = "领取储蓄罐成长值"; break;
                    default: type = "其他活动";
                }
                Integer add = singleJsonObject.getInteger("finaladd");
                allAdd += add;
                sb.append(type).append("->").append(add).append("\n");
            }
            return sb.append("总成长值->").append(allAdd).toString();
        }else return "获取失败，请更新QQ！";
    }

    @Override
    public String publishNotice(QqLoginEntity QqLoginEntity, Long group, String text) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("qid", group.toString());
        map.put("bkn", QqLoginEntity.getGtk());
        map.put("text", text);
        map.put("pinned", "0");
        map.put("type", "1");
        map.put("settings", "{\"is_show_edit_card\":0,\"tip_window_type\":1,\"confirm_required\":0}");
        JSONObject jsonObject = OkHttpUtils.postJson("https://web.qun.qq.com/cgi-bin/announce/add_qun_notice", map,
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0: return "发公告成功！！";
            case 35: return "我还不是管理员呢，不能发送公告！";
            case 1: return "发送公告失败，请更新QQ！";
            default: return "发公告失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public String getGroupLink(QqLoginEntity QqLoginEntity, Long group) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("gc", group.toString());
        map.put("type", "1");
        map.put("bkn", QqLoginEntity.getGtk());
        JSONObject jsonObject = OkHttpUtils.postJson("https://admin.qun.qq.com/cgi-bin/qun_admin/get_join_link", map,
                OkHttpUtils.addHeaders(QqLoginEntity.getCookie(), "https://admin.qun.qq.com/create/share/index.html?ptlang=2052&groupUin=" + group));
        switch (jsonObject.getInteger("ec")){
            case 0: return jsonObject.getString("url");
            case 1: return "获取链接失败，请更新QQ！！";
            default: return "加群链接获取失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public String groupActive(QqLoginEntity QqLoginEntity, Long group, Integer page) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://qqweb.qq.com/c/activedata/get_mygroup_data?bkn=%s&gc=%d&page=%d",
                QqLoginEntity.getGtk(), group, page), OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        if (jsonObject.getInteger("ec") == 0){
            JSONArray jsonArray = jsonObject.getJSONObject("ginfo").getJSONArray("g_most_act");
            if (jsonArray != null){
                StringBuilder sb = new StringBuilder();
                jsonArray.forEach(obj -> {
                    JSONObject singleJsonObject = (JSONObject) obj;
                    sb.append("@").append(singleJsonObject.getString("name")).append(":").append(singleJsonObject.getString("sentences_num")).append("条").append("\n");
                });
                return sb.deleteCharAt(sb.length() - 1).toString();
            }else return "群活跃数据获取失败！可能没有活跃信息！";
        }else return "群活跃数据获取失败，请更新QQ！";
    }

    @Override
    public String allShutUp(QqLoginEntity QqLoginEntity, Long group, Boolean isShutUp) throws IOException {
        long num = 0L;
        if (isShutUp) num = 4294967295L;
        Map<String, String> map = new HashMap<>();
        map.put("src", "qinfo_v3");
        map.put("gc", group.toString());
        map.put("bkn", QqLoginEntity.getGtk());
        map.put("all_shutup", String.valueOf(num));
        JSONObject jsonObject = OkHttpUtils.postJson("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_shutup", map,
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0:
                if (isShutUp) return "全体禁言成功！！";
                else return "解除全体禁言成功";
            case 7: return "权限不够，我无法执行！！";
            case -100005: return "群号不存在";
            case 4: return "执行失败，请更新QQ！！";
            default: return "执行失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public String changeName(QqLoginEntity QqLoginEntity, Long qq, Long group, String name) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("u", qq.toString());
        map.put("name", name);
        map.put("gc", group.toString());
        map.put("bkn", QqLoginEntity.getGtk());
        map.put("src", "qinfo_v3");
        JSONObject jsonObject = OkHttpUtils.postJson("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_card", map,
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0: return "改名片成功！";
            case 3: return "权限不够，我无法执行！";
            case 2:
            case -100005: return "群号不存在！！";
            case 4: return "执行失败，请更新QQ！！";
            default: return "执行失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public String setGroupAdmin(QqLoginEntity QqLoginEntity, Long qq, Long group, Boolean isAdmin) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("u", qq.toString());
        int op = 0;
        if (isAdmin) op = 1;
        map.put("op", String.valueOf(op));
        map.put("gc", group.toString());
        map.put("bkn", QqLoginEntity.getGtk());
        map.put("src", "qinfo_v3");
        JSONObject jsonObject = OkHttpUtils.postJson("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_admin", map,
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0:
                if (isAdmin) return "添加" + qq + "为管理员成功";
                else return "取消" + qq + "的管理员成功";
            case 7: return "权限不够，我无法执行！！";
            case 2:
            case -100005: return "群号不存在！！";
            case 4: return "执行失败，请更新QQ！！";
            default: return "执行失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public String growthLike(QqLoginEntity QqLoginEntity) throws IOException {
        Result<String> result = QqSuperLoginUtils.vipLogin(QqLoginEntity);
        if (result.getCode() == 200){
            String psKey = result.getData();
            String url = String.format("https://mq.vip.qq.com/m/growth/rank?ADTAG=vipcenter&_wvSb=1&traceNum=2&traceId=%s%s",
                    QqLoginEntity.getQqEntity().getQq(), String.valueOf(System.currentTimeMillis()).substring(0, 11));
            Response response = OkHttpUtils.get(url, OkHttpUtils.addCookie(QqLoginEntity.getCookie(psKey)));
            if (response.code() == 200){
                String html = OkHttpUtils.getStr(response);
                Elements elements = Jsoup.parse(html).getElementsByClass("f-uin");
                for (Element ele: elements){
                    String toUin = ele.text();
                    if (String.valueOf(QqLoginEntity.getQqEntity().getQq()).equals(toUin)) continue;
                    JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://mq.vip.qq.com/m/growth/doPraise?method=0&toUin=%s&g_tk=%s&ps_tk=%s",
                            toUin, QqUtils.getGTK2(QqLoginEntity.getSKey()), QqLoginEntity.getGtk(psKey)),
                            OkHttpUtils.addHeaders(QqLoginEntity.getCookie(psKey), url));
                    Integer code = jsonObject.getInteger("ret");
                    if (code != -12002 && code != 0){
                        return "点赞失败！！" + jsonObject.getString("msg");
                    }
                }
                return "排行榜点赞成功！！";
            }else {
                response.close();
                return "访问排行榜点赞页面失败，请稍后再试！！";
            }
        }else return "您的QQ已失效，请更新QQ！！";
    }

    @Override
    public Result<List<GroupMember>> groupMemberInfo(QqLoginEntity QqLoginEntity, Long group) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://qinfo.clt.qq.com/cgi-bin/qun_info/get_members_info_v1?friends=1&gc=%s&bkn=%s&src=qinfo_v3&_ti=%s",
                group, QqLoginEntity.getGtk(), System.currentTimeMillis()), OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0:
                JSONObject membersJsonObject = jsonObject.getJSONObject("members");
                List<GroupMember> list = new ArrayList<>();
                for (Map.Entry<String, Object> entry: membersJsonObject.entrySet()){
                    JSONObject memberJsonObject = (JSONObject) entry.getValue();
                    list.add(new GroupMember(Long.parseLong(entry.getKey()), memberJsonObject.getInteger("ll"),
                            memberJsonObject.getInteger("lp"), Long.parseLong(memberJsonObject.getString("jt") + "000"),
                            Long.parseLong(memberJsonObject.getString("lst") + "000")));
                }
                return Result.success(list);
            case 4: return Result.failure("查询失败，请更新QQ！！", null);
            default: return Result.failure("查询失败，" + jsonObject.getString("em"), null);
        }
    }

    @Override
    public String changePhoneOnline(QqLoginEntity QqLoginEntity, String iMei, String phone) throws IOException {
        Result<String> result = QqSuperLoginUtils.vipLogin(QqLoginEntity);
        if (result.getCode() == 200){
            String psKey = result.getData();
            JSONObject jsonObject = OkHttpUtils.getJson("https://proxy.vip.qq.com/cgi-bin/srfentry.fcgi?ts=" + System.currentTimeMillis() + "&daid=18&g_tk=" + QqLoginEntity.getGtk(psKey) + "&data=%7B%2213031%22:%7B%22req%22:%7B%22sModel%22:%22" + phone + "%22,%22sManu%22:%22vivo%22,%22sIMei%22:%22" + iMei + "%22,%22iAppType%22:3,%22sVer%22:%228.4.1.4680%22,%22lUin%22:" + QqLoginEntity.getQqEntity().getQq() + ",%22bShowInfo%22:true,%22sDesc%22:%22%22,%22sModelShow%22:%22" + phone + "%22%7D%7D%7D&pt4_token=" + QqLoginEntity.getPt4Token(),
                    OkHttpUtils.addCookie(QqLoginEntity.getCookie(psKey)));
            if (jsonObject.getInteger("ecode") == 0) return "修改在线手机型号成功！！";
            else if (jsonObject.getInteger("ecode") == -500000) return "修改失败，请更新QQ！！";
            else if (jsonObject.getInteger("ret") == -100) return "修改失败，请更新QQ！！";
            else {
                String msg = jsonObject.getString("msg");
                if (msg == null) jsonObject.getJSONObject("13031").getString("msg");
                return "修改失败，" + msg;
            }
        }else return "修改失败，请更新QQ！！";
    }

    @Override
    public String queryLevel(QqLoginEntity QqLoginEntity, Long qq, String psKey) throws IOException {
        if (psKey == null){
            Result<String> result = QqSuperLoginUtils.vipLogin(QqLoginEntity);
            if (result.getCode() == 200) psKey = result.getData();
            else return result.getMessage();
        }
        Response response = OkHttpUtils.get("https://club.vip.qq.com/api/vip/getQQLevelInfo?requestBody=%7B%22sClientIp%22:%22127.0.0.1%22,%22sSessionKey%22:%22" + QqLoginEntity.getSKey() + "%22,%22iKeyType%22:1,%22iAppId%22:0,%22iUin%22:" + QqLoginEntity.getQqEntity().getQq() + "%7D",
                OkHttpUtils.addCookie(QqLoginEntity.getCookie(psKey)));
        if (response.code() == 200){
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            return jsonObject.getJSONObject("data").getJSONObject("mRes").getString("iQQLevel");
        }else return "获取等级失败，请更新QQ！！";
    }

    @Override
    public List<Map<String, String>> getGroupMsgList(QqLoginEntity QqLoginEntity) throws IOException {
        String html = OkHttpUtils.getStr("https://web.qun.qq.com/cgi-bin/sys_msg/getmsg?ver=5761&filter=0&ep=0",
                OkHttpUtils.addCookie(QqLoginEntity.getCookie()));
        Elements elements = Jsoup.parse(html).getElementById("msg_con").getElementsByTag("dd");
        List<Map<String, String>> list = new ArrayList<>();
        out:for (Element ele: elements){
            Element ddEle = ele.getElementsByTag("dd").first();
            int isOp = 1;
            try {
                int opBtn = ddEle.getElementsByClass("btn_group").first().children().size();
                if (opBtn == 0) isOp = 0;
            }catch (NullPointerException e){
                isOp = 0;
            }
            int typeInt = Integer.parseInt(ele.attr("type"));
            String type;
            switch (typeInt){
                case 1: type = "apply"; break;
                case 2: type = "beInvite"; break;
                case 13: type = "leave"; break;
                case 22: type = "invite"; break;
                case 3: type = "addManager"; break;
                case 60: type = "payAdd"; break;
                default: continue out;
            }
            String seq = ddEle.attr("seq");
            String group = ddEle.attr("qid");
            String authKey = ddEle.attr("authKey");
            Elements liElements = ele.getElementsByTag("li");
            Element liEle = liElements.first();
            String msg = liEle.attr("aria-label");
            String qq = liEle.getElementsByTag("a").first().attr("uin");
            Map<String, String> map = new HashMap<>();
            map.put("seq", seq);
            map.put("group", group);
            map.put("authKey", authKey);
            map.put("msg", msg);
            map.put("qq", qq);
            map.put("type", type);
            map.put("isOp", String.valueOf(isOp));
            map.put("typeInt", String.valueOf(typeInt));
            if (liElements.size() != 1){
                Element secondLiEle = liElements.get(1);
                if (secondLiEle.getElementsByClass("apply_add_msg").size() == 0){
                    map.put("inviteMsg", secondLiEle.attr("aria-label"));
                    Element memberEle = secondLiEle.getElementsByTag("a").first();
                    map.put("inviteQQ", memberEle.attr("uin"));
                    map.put("inviteName", memberEle.text());
                }else {
                    map.put("applyMsg", secondLiEle.attr("title"));
                }
            }
            list.add(map);
        }
        return list;
    }

    @Override
    public String operatingGroupMsg(QqLoginEntity QqLoginEntity, String type, Map<String, String> map, String refuseMsg) throws IOException {
        String url = "https://web.qun.qq.com/cgi-bin/sys_msg/set_msgstate";
        Headers cookie = OkHttpUtils.addCookie(QqLoginEntity.getCookie());
        FormBody.Builder builder = new FormBody.Builder()
                .add("seq", map.get("seq"))
                .add("t", map.get("typeInt"))
                .add("gc", map.get("group"))
                .add("uin", String.valueOf(QqLoginEntity.getQqEntity().getQq()))
                .add("ver", "false")
                .add("from", "2")
                .add("bkn", QqLoginEntity.getGtk());
        switch (type){
            case "ignore":
                JSONObject jsonObject = OkHttpUtils.postJson(url, builder.add("cmd", "3").build(), cookie);
                if (jsonObject.getInteger("ec") == 0) return "忽略加入群聊成功！！";
                else return jsonObject.getString("em");
            case "refuse":
                if (refuseMsg == null) refuseMsg = "这是一条拒绝的消息哦！！";
                jsonObject = OkHttpUtils.postJson(url, builder.add("msg", refuseMsg).add("flag", "0").build(),
                        cookie);
                if (jsonObject.getInteger("ec") == 0) return "拒绝加入群聊成功！！";
                else return jsonObject.getString("em");
            case "agree":
                jsonObject = OkHttpUtils.postJson(url, builder.add("cmd", "1").build(), cookie);
                if (jsonObject.getInteger("ec") == 0) return "同意加入群聊成功！！";
                else return jsonObject.getString("em");
            default: return "类型不匹配！！";
        }
    }

    @Override
    public Result<String> videoSign(QqVideoEntity qqVideoEntity) throws IOException {
        Response response = OkHttpUtils.get("https://access.video.qq.com/user/auth_refresh?vappid=11059694&vsecret=fdf61a6be0aad57132bc5cdf78ac30145b6cd2c1470b0cfe&type=qq&g_tk=&g_vstk=" + QqUtils.getGTK(qqVideoEntity.getVuSession()) + "&g_actk=" + QqUtils.getGTK(qqVideoEntity.getAccessToken()) + "&callback=jQuery19107785201373825752_1626233689988&_=" + System.currentTimeMillis(),
                OkHttpUtils.addHeaders(qqVideoEntity.getCookie(), "https://v.qq.com/", UA.PC));
        response.close();
        if (response.code() == 200){
            String cookie = OkHttpUtils.getCookie(response);
            String vuSession = OkHttpUtils.getCookie(cookie, "vqq_vusession");
            String accessToken = OkHttpUtils.getCookie(cookie, "vqq_access_token");
            qqVideoEntity.setCookie(cookie);
            qqVideoEntity.setVuSession(vuSession);
            qqVideoEntity.setAccessToken(accessToken);
            qqVideoService.save(qqVideoEntity);
            Headers headers = OkHttpUtils.addHeaders(cookie, "https://v.qq.com/", UA.PC);
            JSONObject jsonObject = OkHttpUtils.getJsonp("https://vip.video.qq.com/fcgi-bin/comm_cgi?name=spp_MissionFaHuo&cmd=4&task_id=7&_=1582364733058&callback=video",
                    headers);
            OkHttpUtils.getJsonp("https://vip.video.qq.com/fcgi-bin/comm_cgi?name=spp_MissionFaHuo&cmd=4&task_id=6&_=1582366326994&callback=video",
                    headers);
            OkHttpUtils.getJsonp("https://vip.video.qq.com/fcgi-bin/comm_cgi?name=hierarchical_task_system&cmd=2&_=1555060502385&callback=video",
                    headers);
            OkHttpUtils.getJsonp("https://vip.video.qq.com/fcgi-bin/comm_cgi?name=spp_MissionFaHuo&cmd=4&task_id=3&_=1582368319252&callback=video",
                    headers);
            OkHttpUtils.getJsonp("https://vip.video.qq.com/fcgi-bin/comm_cgi?name=spp_MissionFaHuo&cmd=4&task_id=1&_=1582997048625&callback=video",
                    headers);
            return Result.success("腾讯视频签到成功！", null);
        }else return Result.failure("腾讯视频签到失败，cookie已失效，请重新登录！");
    }
}
