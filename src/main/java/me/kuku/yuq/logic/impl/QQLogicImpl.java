package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQLogic;
import me.kuku.yuq.pojo.GroupMember;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import me.kuku.yuq.utils.QQSuperLoginUtils;
import me.kuku.yuq.utils.QQUtils;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class QQLogicImpl implements QQLogic {
    @Override
    public Result<Map<String, String>> groupUploadImage(QQLoginEntity qqLoginEntity, String url) throws IOException {
        byte[] bytes = OkHttpUtils.getBytes(url);
        MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("bkn", qqLoginEntity.getGtk())
                .addFormDataPart("pic_up", Base64.getEncoder().encodeToString(bytes)).build();
        JSONObject jsonObject = OkHttpUtils.postJson("https://qun.qq.com/cgi-bin/qiandao/upload/pic", body,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        if (jsonObject.getInteger("retcode").equals(0)){
            JSONObject dataJsonObject = jsonObject.getJSONObject("data");
            Map<String, String> map = new HashMap<>();
            map.put("picId", dataJsonObject.getString("pic_id"));
            map.put("picUrl", dataJsonObject.getString("pic_url"));
            return Result.success(map);
        }else return Result.failure("上传图片失败，" + jsonObject.getString("msg"), null);
    }

    @Override
    public String groupLottery(QQLoginEntity qqLoginEntity, Long group) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("bkn", qqLoginEntity.getGtk());
        map.put("from", "0");
        map.put("gc", group.toString());
        map.put("client", "1");
        map.put("version", "8.3.0.4480");
        JSONObject jsonObject = OkHttpUtils.postJson("https://pay.qun.qq.com/cgi-bin/group_pay/good_feeds/draw_lucky_gift", map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        if (jsonObject.getInteger("ec").equals(0)) {
            if (jsonObject.getInteger("lucky_code") == 7779) return "抱歉，等级不够41级，无法抽礼物";
            else if ("".equals(jsonObject.getString("name")) || jsonObject.getString("name") == null) return "抱歉，没有抽到礼物！！";
            else return "抽礼物成功，抽到了" + jsonObject.getString("name");
        }else return "抽礼物失败，请更新QQ！！";
    }

    @Override
    public String vipSign(QQLoginEntity qqLoginEntity) throws IOException {
        StringBuilder sb = new StringBuilder();
        String gtk2 = qqLoginEntity.getGtk2();
        Headers cookie = OkHttpUtils.addCookie(qqLoginEntity.getCookie());
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=79968&format=json&g_tk=%s&cachetime=%d", gtk2, new Date().getTime()), cookie);
        switch (jsonObject.getInteger("ret")){
            case 0: sb.append("会员面板签到成功！！\n"); break;
            case 10601: sb.append("会员面板今天已经签到！\n"); break;
            case 10002: sb.append("会员面板签到失败！请更新QQ！\n"); break;
            case 20101: sb.append("会员面板签到失败，不是QQ会员！\n"); break;
        }
        jsonObject = OkHttpUtils.getJson(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=403490&rand=0.27489888%s&g_tk=%s&format=json", new Date().getTime(), gtk2), cookie);
        switch (jsonObject.getInteger("ret")){
            case 0: sb.append("会员电脑端签到成功！！\n"); break;
            case 10601: sb.append("会员电脑端今天已经签到！\n"); break;
            case 10002: sb.append("会员电脑端签到失败！请更新QQ！\n"); break;
        }
        jsonObject = OkHttpUtils.getJson(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?actid=52002&rand=0.27489888%s&g_tk=%s", new Date().getTime(), gtk2), cookie);
        switch (jsonObject.getInteger("ret")){
            case 0: sb.append("会员手机端签到成功！！\n"); break;
            case 10601: sb.append("会员手机端今天已经签到！\n"); break;
            case 10002: sb.append("会员手机端签到失败！请更新QQ！\n"); break;
        }
        jsonObject = OkHttpUtils.getJson(String.format("https://iyouxi4.vip.qq.com/ams3.0.php?_c=page&actid=239151&isLoadUserInfo=1&format=json&g_tk=%s", gtk2), cookie);
        switch (jsonObject.getInteger("ret")){
            case 0: sb.append("会员积分签到成功！！\n"); break;
            case 10601: sb.append("会员积分今天已经签到！\n"); break;
            case 10002: sb.append("会员积分签到失败！请更新QQ！\n"); break;
        }
        jsonObject = OkHttpUtils.getJson("https://iyouxi3.vip.qq.com/ams3.0.php?_c=page&actid=23074&format=json&g_tk=" + gtk2, cookie);
        switch (jsonObject.getInteger("ret")){
            case 0: sb.append("会员积分手机端签到成功！！\n"); break;
            case 10601: sb.append("会员积分手机端今天已经签到！\n"); break;
            case 10002: sb.append("会员积分手机端签到失败！请更新QQ！\n"); break;
        }
        jsonObject = OkHttpUtils.getJson("https://pay.qun.qq.com/cgi-bin/group_pay/good_feeds/gain_give_stock?gain=1&bkn=" + qqLoginEntity.getGtk(),
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "https://m.vip.qq.com/act/qun/jindou.html"));
        switch (jsonObject.getInteger("ec")){
            case 0: sb.append("免费领金豆成功！！\n"); break;
            case 1010: sb.append("今天已经领取过金豆了！\n"); break;
            default: sb.append("领取金豆失败！！");
        }
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=27754&_=%d", gtk2, new Date().getTime()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=27754&_=%d", gtk2, new Date().getTime()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=22894&_c=page&_=%d", gtk2, new Date().getTime()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi4.vip.qq.com/ams3.0.php?g_tk=%s&actid=239371&_c=page&format=json&_=%d", gtk2, new Date().getTime()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=22887&_c=page&format=json&_=%d", gtk2, new Date().getTime()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=202041&_c=page&format=json&_=%d", gtk2, new Date().getTime()), cookie).close();
        OkHttpUtils.get(String.format("https://iyouxi3.vip.qq.com/ams3.0.php?g_tk=%s&actid=202049&_c=page&format=json&_=%d", gtk2, new Date().getTime()), cookie).close();
        return sb.toString();
    }

    @Override
    public String queryVip(QQLoginEntity qqLoginEntity) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://api.unipay.qq.com/v1/r/1450000172/wechat_query?cmd=7&pf=vip_m-50000-html5&pfkey=pfkey&session_id=uin&expire_month=0&session_type=skey&openid=%s&openkey=%s&format=jsonp__myserviceIcons", qqLoginEntity.getQq(), qqLoginEntity.getSKey()),
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
    public String phoneGameSign(QQLoginEntity qqLoginEntity) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson("http://reader.sh.vip.qq.com/cgi-bin/common_async_cgi?g_tk=" + qqLoginEntity.getGtkP() + "&plat=1&version=6.6.6&param=%7B%22key0%22%3A%7B%22param%22%3A%7B%22bid%22%3A13792605%7D%2C%22module%22%3A%22reader_comment_read_svr%22%2C%22method%22%3A%22GetReadAllEndPageMsg%22%7D%7D",
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("ecode").equals(0)) return "手游加速0.2天成功！";
        else return "手游加速失败，请更新QQ！";
    }

    @Override
    public String yellowSign(QQLoginEntity qqLoginEntity) throws IOException {
        StringBuilder sb = new StringBuilder();
        String gtkP = qqLoginEntity.getGtkP();
        Map<String, String> map = new HashMap<>();
        map.put("uin", qqLoginEntity.getQq().toString());
        map.put("format", "json");
        JSONObject jsonObject = OkHttpUtils.postJson(String.format("https://vip.qzone.qq.com/fcg-bin/v2/fcg_mobile_vip_site_checkin?t=0.89457%d&g_tk=%s&qzonetoken=423659183", new Date().getTime(), gtkP),
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        switch (jsonObject.getInteger("code")){
            case 0: sb.append("黄钻签到成功！"); break;
            case -3000: sb.append("黄钻签到失败！请更新QQ！"); break;
            default: sb.append("黄钻今日已签到！！");
        }
        map.clear();
        map.put("option", "sign");
        map.put("uin", qqLoginEntity.getQq().toString());
        map.put("format", "json");
        jsonObject = OkHttpUtils.postJson(String.format("https://activity.qzone.qq.com/fcg-bin/fcg_huangzuan_daily_signing?t=0.%s906035&g_tk=%s&qzonetoken=-1", new Date().getTime(), gtkP),
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        switch (jsonObject.getInteger("code")){
            case 0: sb.append("黄钻公众号签到成功！"); break;
            case -3000: sb.append("黄钻公众号签到失败！请更新QQ！"); break;
            case -90002: sb.append("抱歉，您不是黄钻用户，签到失败"); break;
            default: sb.append("黄钻今日已签到！");
        }
        return sb.toString();
    }

    @Override
    public String qqVideoSign1(QQLoginEntity qqLoginEntity) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://vip.video.qq.com/fcgi-bin/comm_cgi?name=hierarchical_task_system&cmd=2&_=%s8906", new Date().getTime()),
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ret")){
            case 0: return "腾讯视频会员签到成功";
            case -10006: return "腾讯视频会员签到失败，请更新QQ！";
            case -10019: return "您不是腾讯视频会员，签到失败！";
            default: return "腾讯视频会员签到失败，" + jsonObject.getString("msg");
        }
    }

    @Override
    public String qqVideoSign2(QQLoginEntity qqLoginEntity) throws IOException {
        Response response = OkHttpUtils.get("https://access.video.qq.com/user/auth_login?vappid=11059694&vsecret=fdf61a6be0aad57132bc5cdf78ac30145b6cd2c1470b0cfe&login_flag=1&type=qq&appid=101483052&g_tk=" + qqLoginEntity.getGtk() + "&g_vstk=&g_actk=&callback=jQuery19107079438303985055_1588043611061&_=" + new Date().getTime(),
                OkHttpUtils.addCookie(qqLoginEntity.getCookie() + "video_guid=87f1f5fd3c3ebf5a; video_platform=2; "));
        response.close();
        String cookie = OkHttpUtils.getCookie(response);
        if (cookie.contains("vusession")){
            String html = OkHttpUtils.getStr("https://v.qq.com/x/bu/mobile_checkin",
                    OkHttpUtils.addCookie(qqLoginEntity.getCookie() + cookie + "video_guid=fd42304ceeead2c8; video_platform=2; "));
            if (!html.contains("签到失败")) return "签到成功！！";
            else return "签到失败，请先去腾讯视频app私信\"https://v.qq.com/x/bu/mobile_checkin\"并打开该链接";
        }else return "腾讯视频二次签到失败！请更新QQ！";
    }

    @Override
    public String sVipMornSign(QQLoginEntity qqLoginEntity) throws IOException {
        String str = OkHttpUtils.getStr(String.format("https://mq.vip.qq.com/m/signsport/signSport?uin=%d&isRemind=0&ps_tk=%s&g_tk=%s", qqLoginEntity.getQq(), qqLoginEntity.getGtkP(), qqLoginEntity.getGtk2()),
                OkHttpUtils.addHeaders(qqLoginEntity.getCookieWithQQZone(), "https://mq.vip.qq.com/m/signsport/index"));
        if (str.contains("html")) return "sVip打卡报名失败，请更新QQ！";
        else {
            JSONObject jsonObject = JSON.parseObject(str);
            if (jsonObject.getInteger("ret").equals(0)){
                switch (jsonObject.getJSONObject("data").getInteger("code")){
                    case 0: return "sVip打卡报名成功！";
                    case 130002: return "sVip打卡已报名！";
                    default: return "sVip打卡报名失败，" + jsonObject.getString("msg");
                }
            }else return "sVip打卡报名失败，请更新QQ！";
        }
    }

    @Override
    public String sVipMornClock(QQLoginEntity qqLoginEntity) throws IOException {
        Response response = OkHttpUtils.get(String.format("https://mq.vip.qq.com/m/signsport/callSport?uin=%d&g_tk=%s&ps_tk=%s", qqLoginEntity.getQq(), qqLoginEntity.getGtk2(), qqLoginEntity.getGtk2()),
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (response.code() == 200){
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            if (jsonObject.getInteger("ret").equals(0)){
                switch (jsonObject.getJSONObject("data").getInteger("code")){
                    case 0: return "sVip打卡成功";
                    case 100001: return "sVip今日已打卡";
                    default: return "sVip打卡失败，" + jsonObject.getString("msg");
                }
            }else return "sVip打卡失败，请更新QQ！";
        }else {
            response.close();
            return "sVip打卡失败，请更新QQ！";
        }
    }

    @Override
    public String bigVipSign(QQLoginEntity qqLoginEntity) throws IOException {
        OkHttpUtils.get("https://h5.qzone.qq.com/qzone/visitor?_wv=3&_wwv=1024&_proxy=1", OkHttpUtils.addCookie(qqLoginEntity.getCookie())).close();
        Map<String, String> map = new HashMap<>();
        map.put("outCharset", "utf-8");
        map.put("iAppId", "0");
        map.put("llTime", String.valueOf(new Date().getTime()));
        map.put("format", "json");
        map.put("iActionType", "6");
        map.put("strUid", qqLoginEntity.getQq().toString());
        map.put("uin", qqLoginEntity.getQq().toString());
        map.put("inCharset", "utf-8");
        JSONObject jsonObject1 = OkHttpUtils.postJson("https://h5.qzone.qq.com/webapp/json/QQBigVipTask/CompleteTask?t=0." + new Date().getTime() + "906319&g_tk=" + qqLoginEntity.getGtkP(),
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        map.clear();
        map.put("appid", "qq_big_vip");
        map.put("op", "CheckIn");
        map.put("uin", qqLoginEntity.getQq().toString());
        map.put("format", "json");
        map.put("inCharset", "utf-8");
        map.put("outCharset", "utf-8");
        JSONObject jsonObject2 = OkHttpUtils.postJson("https://vip.qzone.qq.com/fcg-bin/v2/fcg_vip_task_checkin?t=0" + new Date().getTime() + "082161&g_tk=" + qqLoginEntity.getGtkP(), map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject1.getInteger("ret") == 0 && jsonObject2.getInteger("code") == 0) return "大会员签到成功！！";
        else if (jsonObject1.getInteger("ret") == -3000 && jsonObject2.getInteger("code") == -3000) return "大会员签到失败！请更新QQ！";
        else return "大会员签到失败！！";
    }

    @Override
    public String modifyNickname(QQLoginEntity qqLoginEntity, String nickname) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("qzreferrer", "http%3A%2F%2Fctc.qzs.qq.com%2Fqzone%2Fv6%2Fsetting%2Fprofile%2Fprofile.html%3Ftab%3Dbase");
        map.put("nickname", nickname);
        map.put("emoji", "");
        map.put("sex", "");
        map.put("birthday", "");
        map.put("province", "0");
        map.put("city", "0");
        map.put("country", "");
        map.put("marriage", "6");
        map.put("bloodtype", "5");
        map.put("hp", "0");
        map.put("hc", "");
        map.put("hco", "");
        map.put("career", "");
        map.put("company", "");
        map.put("cp", "0");
        map.put("cc", "0");
        map.put("cb", "0");
        map.put("cco", "0");
        map.put("lover", "");
        map.put("islunar", "0");
        map.put("mb", "1");
        map.put("uin", qqLoginEntity.getQq().toString());
        map.put("pageindex", "1");
        map.put("nofeeds", "1");
        map.put("fupdate", "1");
        map.put("format", "json");
        JSONObject jsonObject = OkHttpUtils.postJson("https://w.qzone.qq.com/cgi-bin/user/cgi_apply_updateuserinfo_new?g_tk=" + qqLoginEntity.getGtkP(), map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("code") == 0) return "\"修改昵称成功！当前昵称：" + nickname;
        else return "修改昵称失败！" + jsonObject.getString("msg");
    }

    @Override
    public String modifyAvatar(QQLoginEntity qqLoginEntity, String url) throws IOException {
        MultipartBody multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("is_set", "1")
                .addFormDataPart("is_share", "0")
                .addFormDataPart("format", "png")
                .addFormDataPart("name", "")
                .addFormDataPart("vip_level", "0")
                .addFormDataPart("isHD", "false")
                .addFormDataPart("catId", "0")
                .addFormDataPart("cmd", "set_and_share_face")
                .addFormDataPart("Filename", "image100*100")
                .addFormDataPart("Upload", "Submit Query")
                .addFormDataPart("Filedata[]", "image100*100", OkHttpUtils.addStream(OkHttpUtils.getByteStr(url)))
                .build();
        JSONObject jsonObject = OkHttpUtils.postJson("https://face.qq.com/client/uploadflash.php", multipartBody, OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        switch (jsonObject.getInteger("result")){
            case 0: return "QQ头像设置成功！";
            case 1001: return "头像设置失败，请更新QQ！";
            case 1002: return "非QQ会员，无法设置头像！";
            case 2004: return "图片不规范，换个图片吧！";
            default: return "头像设置失败！！";
        }
    }

    @Override
    public String weiYunSign(QQLoginEntity qqLoginEntity) throws IOException {
        Result<String> result = QQSuperLoginUtils.weiYunLogin(qqLoginEntity);
        if (result.getCode() == 200){
            String psKey = result.getData();
            String str = OkHttpUtils.getStr("https://h5.weiyun.com/sign_in", OkHttpUtils.addCookie(qqLoginEntity.getCookie(psKey)));
            String json = BotUtils.regex("(?<=window\\.__INITIAL_STATE__=).+?(?=</script>)", str);
            JSONObject jsonObject = JSON.parseObject(json);
            return String.format("微云签到成功，已连续签到%d天，当前金币%d",
                    jsonObject.getJSONObject("index").getInteger("consecutiveSignInCount"), jsonObject.getJSONObject("global").getInteger("totalCoin"));
        }else return "微云签到失败，请更新QQ！";
    }

    @Override
    public String qqMusicSign(QQLoginEntity qqLoginEntity) throws IOException {
        StringBuilder sb = new StringBuilder();
        String url = "https://u.y.qq.com/cgi-bin/musicu.fcg";
        Headers headers = OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), url);
        String gtk = qqLoginEntity.getGtk();
        JSONObject jsonObject = OkHttpUtils.postJson(url,
                OkHttpUtils.addJson("{\"req_0\":{\"module\":\"UserGrow.UserGrowScore\",\"method\":\"receive_score\",\"param\":{\"musicid\":\"" + qqLoginEntity.getQq() + "\",\"type\":15}},\"comm\":{\"g_tk\":" + gtk + ",\"uin\":" + qqLoginEntity.getQq() + ",\"format\":\"json\",\"ct\":23,\"cv\":0}}"),
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
                OkHttpUtils.addJson("{\"comm\":{\"g_tk\":" + gtk + ",\"uin\":" + qqLoginEntity.getQq() + ",\"format\":\"json\",\"inCharset\":\"utf-8\",\"outCharset\":\"utf-8\",\"notice\":0,\"platform\":\"h5\",\"needNewCode\":1,\"ct\":23,\"cv\":0},\"req_0\":{\"module\":\"music.activeCenter.ActiveCenterSignSvr\",\"method\":\"DoSignIn\",\"param\":{}}}"),
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
                OkHttpUtils.addJson("{\"req_0\":{\"module\":\"UserGrow.UserGrowScore\",\"method\":\"receive_score\",\"param\":{\"musicid\":\"" + qqLoginEntity.getQq() + "\",\"type\":1}},\"comm\":{\"g_tk\":" + gtk + ",\"uin\":" + qqLoginEntity.getQq() + ",\"format\":\"json\",\"ct\":23,\"cv\":0}}"),
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
                OkHttpUtils.addJson("{\"req_0\":{\"module\":\"Radio.RadioLucky\",\"method\":\"clockIn\",\"param\":{\"platform\":2}},\"comm\":{\"g_tk\":" + qqLoginEntity.getGtkP() + ",\"uin\":" + qqLoginEntity.getQq() + ",\"format\":\"json\"}}"),
                headers);
        jsonObject = jsonObject.getJSONObject("req_0").getJSONObject("data");
        switch (jsonObject.getInteger("retCode")){
            case 0: sb.append("QQ音乐电台锦鲤打卡成功！积分+").append(jsonObject.getString("score")); break;
            case 40001: sb.append("QQ音乐电台锦鲤已打卡！"); break;
            case -13004: sb.append("QQ音乐电台锦鲤打卡失败！请更新QQ！"); break;
            default: sb.append("QQ音乐电台锦鲤打卡失败！").append(jsonObject.getString("errMsg")); break;
        }
        OkHttpUtils.get(String.format("https://service-n157vbwh-1252343050.ap-beijing.apigateway.myqcloud.com/release/lzz_qqmusic?qq=%d&hour=2", qqLoginEntity.getQq())).close();
        return sb.toString();
    }

    @Override
    public String gameSign(QQLoginEntity qqLoginEntity) throws IOException {
        String gtk = qqLoginEntity.getGtk();
        StringBuilder sb = new StringBuilder();
        String qq = qqLoginEntity.getQq().toString();
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("http://social.minigame.qq.com/cgi-bin/social/welcome_panel_operate?format=json&cmd=2&uin=%s&g_tk=%s", qq, gtk),
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "http://minigame.qq.com/appdir/social/cloudHall/src/index/welcome.html"));
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
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "http://minigame.qq.com/appdir/social/cloudHall/src/index/welcome.html"));
        switch (jsonObject.getInteger("result")){
            case 0: sb.append("游戏大厅2签到成功！！\n"); break;
            case 1000005: sb.append("游戏大厅2签到失败！请更新QQ！\n"); break;
            default: sb.append("游戏大厅签到2失败！").append(jsonObject.getString("resultstr")).append("\n");
        }
        Response response = OkHttpUtils.get("http://info.gamecenter.qq.com/cgi-bin/gc_my_tab_async_fcgi?merge=1&ver=0&st=" + new Date().getTime() + "746&sid=&uin=" + qq + "&number=0&path=489&plat=qq&gamecenter=1&_wv=1031&_proxy=1&gc_version=2&ADTAG=gamecenter&notShowPub=1&param=%7B%220%22%3A%7B%22param%22%3A%7B%22platform%22%3A1%2C%22tt%22%3A1%7D%2C%22module%22%3A%22gc_my_tab%22%2C%22method%22%3A%22sign_in%22%7D%7D&g_tk=" + gtk,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
                "&g_tk=$gtk&_t=0.6780016267291531", OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        String jsonStr = BotUtils.regex("(?<=var sign_index = ).*?(?=;)", OkHttpUtils.getStr(response));
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
                map, OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
        jsonObject = OkHttpUtils.getJson(dnfUrl, OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), dnfUrl));
        iRet = jsonObject.getInteger("iRet");
        if (iRet == 0){
            if (jsonObject.getJSONObject("jData").getInteger("iLotteryRet") == 100002) sb.append("DNF社区积分已领取！");
            else sb.append("DNF社区积分领取成功！");
        }else sb.append("DNF社区积分领取失败！");
        return sb.toString();
    }

    @Override
    public String qPetSign(QQLoginEntity qqLoginEntity) throws IOException {
        Response response = OkHttpUtils.get("https://fight.pet.qq.com/cgi-bin/petpk?cmd=award&op=1&type=0", OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
    public String tribeSign(QQLoginEntity qqLoginEntity) throws IOException {
        StringBuilder sb = new StringBuilder();
        String gtk = qqLoginEntity.getGtk();
        Map<String, String> map = new HashMap<>();
        map.put("bkn", gtk);
        JSONObject jsonObject = OkHttpUtils.postJson("https://buluo.qq.com/cgi-bin/bar/login_present_heart", map,
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "https://buluo.qq.com/mobile/my_heart.html"));
        switch (jsonObject.getInteger("retcode")){
            case 0: {
                if (jsonObject.getJSONObject("result").getInteger("add_hearts") == 0)
                    sb.append("今日已领取爱心");
                else sb.append("成功领取爱心 + ").append(jsonObject.getJSONObject("result").getInteger("add_hearts")).append("，");
                break;
            }
            case 100000: sb.append("领取爱心失败，请更新QQ！");
            default: sb.append("领取爱心失败！");
        }
        Response response = OkHttpUtils.get("https://buluo.qq.com/cgi-bin/bar/card/bar_list_by_page?uin=" + qqLoginEntity.getQq() + "&neednum=30&startnum=0&r=0.98389" + new Date().getTime(),
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "https://buluo.qq.com/mobile/personal.html"));
        if (response.code() != 200){
            response.close();
            return "获取兴趣部落列表失败！";
        }
        JSONObject tribeJsonObject = OkHttpUtils.getJson(response);
        Integer code = tribeJsonObject.getInteger("retcode");
        if (code == 0){
            JSONArray jsonArray = tribeJsonObject.getJSONObject("result").getJSONArray("followbars");
            jsonArray.forEach(obj -> {
                JSONObject singleJsonObject = (JSONObject) obj;
                map.clear();
                map.put("bid", singleJsonObject.getString("bid"));
                map.put("bkn", gtk);
                map.put("r", "0.84746" + new Date().getTime());
                try {
                    JSONObject resultJsonObject = OkHttpUtils.postJson("https://buluo.qq.com/cgi-bin/bar/user/sign", map,
                            OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "https://buluo.qq.com/mobile/personal.html"));
                    sb.append(singleJsonObject.getString("name"));
                    switch (resultJsonObject.getInteger("retcode")){
                        case 0: sb.append("部落签到成功！"); break;
                        case 100000: sb.append("部落签到失败！请更新QQ！"); break;
                        default: sb.append("部落签到失败！");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (code == 100000) sb.append("兴趣部落签到失败！请更新QQ！");
        else sb.append("兴趣部落签到失败！");
        return sb.toString();
    }

    @Override
    public String refuseAdd(QQLoginEntity qqLoginEntity) throws IOException {
        HashMap<String, String> map = new HashMap<>();
        map.put("req", "{\"at\":2,\"q\":\"\",\"a\":\"\",\"l\":[],\"viaphone\":0}");
        map.put("bkn", qqLoginEntity.getGtk());
        JSONObject jsonObject = OkHttpUtils.postJson("https://ti.qq.com/cgi-node/friend-auth/set", map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookieWithQQZone()));
        if (jsonObject.getInteger("ec") == 0) return "设置拒绝任何人添加成功！";
        else return "设置失败，请更新QQ！！";
    }

    @Override
    public String motionSign(QQLoginEntity qqLoginEntity) throws IOException {
        int step = (int) (Math.random() * 88888 + 11111);
        long time = new Date().getTime();
        Map<String, String> map = new HashMap<>();
        map.put("params", String.format("{\"reqtype\":11,\"mbtodayStep\":%d,\"todayStep\":%d,\"timestamp\":%d}", step, step, time));
        map.put("l5apiKey", "daka.server");
        map.put("dcapiKey", "daka_tcp");
        JSONObject jsonObject = OkHttpUtils.postJson("https://yundong.qq.com/cgi/common_daka_tcp?g_tk=" + qqLoginEntity.getGtk(), map,
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), String.format("https://yundong.qq.com/daka/index?_wv=2098179&rank=1&steps=%d&asyncMode=1&type=&mid=105&timestamp=%d", step, time)));
        switch (jsonObject.getInteger("code")){
            case 0: return "QQ运动打卡成功！QQ成长值+0.2天！";
            case -10001: return "今天步数未达到打卡门槛，再接再厉！";
            case -10003: return "QQ运动今日已打卡！";
            case -1001: return "QQ运动打卡失败！请更新QQ！";
            default: return "QQ运动打卡失败！" + jsonObject.getString("emsg");
        }
    }

    @Override
    public String blueSign(QQLoginEntity qqLoginEntity) throws IOException {
        Result<String> result = QQSuperLoginUtils.blueLogin(qqLoginEntity);
        if (result.getCode() == 200){
            String psKey = result.getData();
            String cookie = qqLoginEntity.getCookie(psKey) + "DomainID=176; ";
            String gtk = qqLoginEntity.getGtk();
            StringBuilder sb = new StringBuilder();
            JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://app.gamevip.qq.com/cgi-bin/gamevip_sign/GameVip_SignIn?format=json&g_tk=%s&_=%s", gtk, new Date().getTime()),
                    OkHttpUtils.addHeaders(cookie, "https://gamevip.qq.com/sign_pop/sign_pop_v2.html"));
            switch (jsonObject.getInteger("result")){
                case 0: sb.append("蓝钻签到成功！当前签到积分").append(jsonObject.getString("SignScore")).append("点\n"); break;
                case 1000005: sb.append("蓝钻签到失败，请更新QQ！！\n"); break;
                default: sb.append("蓝钻签到失败！").append(jsonObject.getString("resultstr"));
            }
            jsonObject = OkHttpUtils.getJson(String.format("https://app.gamevip.qq.com/cgi-bin/gamevip_sign/GameVip_Lottery?format=json&g_tk=%s&_=%s0334", gtk, new Date().getTime()),
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
    public String sendFlower(QQLoginEntity qqLoginEntity, Long qq, Long group) throws IOException {
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
        map.put("bkn", qqLoginEntity.getGtk());
        JSONObject jsonObject = OkHttpUtils.postJson("https://pay.qun.qq.com/cgi-bin/group_pay/good_feeds/send_goods", map,
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "https://qun.qq.com/qunpay/gifts/index.html?troopUin=$group&uin=$qq&name=&from=profilecard&_wv=1031&_bid=2204&_wvSb=1&_nav_alpha=0"));
        switch (jsonObject.getInteger("ec")){
            case 0: return "送花成功";
            case 4:
            case 1: return "送花失败，请更新QQ";
            case 20000: return "鲜花不足，充点钱再送把！";
            default: return "送花失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public String anotherSign(QQLoginEntity qqLoginEntity) throws IOException {
        Response response = OkHttpUtils.post("https://ti.qq.com/hybrid-h5/api/json/daily_attendance/SignInMainPage",
                OkHttpUtils.addJson("{\"uin\": \"" + qqLoginEntity.getQq() + "\",\"QYY\": 2,\"qua\": \"V1_AND_SQ_8.3.3_1376_YYB_D\",\"loc\": {\"lat\": 27719813,\"lon\": 111317537}}"),
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        if (response.code() != 200){
            response.close();
            return "打卡失败，请稍后再试！！";
        }
        JSONObject jsonObject = OkHttpUtils.getJson(response);
        if (jsonObject.getInteger("ret") == 0){
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONObject("vecSignInfo").getJSONArray("value");
            String type = null;
            String subType = null;
            for (int i = 0; i < jsonArray.size(); i++){
                JSONObject signJsonObject = jsonArray.getJSONObject(i);
                if ("收集卡".equals(signJsonObject.getJSONObject("signInCover").getString("title"))){
                    if (signJsonObject.getInteger("signInResult") == 1) return "今日已打卡";
                    subType = signJsonObject.getString("subType");
                    type = signJsonObject.getString("type");
                    break;
                }
            }
            if (type != null && subType != null){
                response = OkHttpUtils.post("https://ti.qq.com/hybrid-h5/api/json/daily_attendance/SignIn",
                        OkHttpUtils.addJson(String.format("{\"uin\":\"%d\",\"type\":%s,\"sId\":\"\",\"subType\":%s,\"qua\":\"V1_AND_SQ_8.3.3_1376_YYB_D\"}", qqLoginEntity.getQq(), type, subType)),
                        OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
                if (response.code() != 200) return "打卡失败，请稍后再试！！";
                JSONObject signJsonObject = OkHttpUtils.getJson(response);
                if (signJsonObject.getInteger("ret") == 0){
                    for (int i = 0; i < 3; i++){
                        OkHttpUtils.post("https://ti.qq.com/hybrid-h5/api/json/daily_attendance/AdvNotify",
                                OkHttpUtils.addJson(String.format("{\"uin\":%d}", qqLoginEntity.getQq())),
                                OkHttpUtils.addCookie(qqLoginEntity.getCookie())).close();
                    }
                    return "打卡成功！！";
                }else return "打卡失败！" + signJsonObject.getString("msg");
            }else return "打卡失败，没有发现收集卡";
        }else return "打卡失败，请更新QQ！！";
    }

    private Integer getBubbleId(QQLoginEntity qqLoginEntity, String psKey, String name) throws IOException {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 5; i++){
            map.put("page", String.valueOf(i));
            map.put("num", "15");
            JSONObject jsonObject = OkHttpUtils.postJson(String.format("https://zb.vip.qq.com/bubble/cgi/getDiyBubbleList?daid=18&g_tk=%s&p_tk=%s", qqLoginEntity.getGtk(), qqLoginEntity.getPt4Token()),
                    map, OkHttpUtils.addHeaders(qqLoginEntity.getCookie(psKey) + "pt4_token=" + qqLoginEntity.getPt4Token(), null, UA.QQ));
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
    public String diyBubble(QQLoginEntity qqLoginEntity, String text, String name) throws IOException {
        Result<String> result = QQSuperLoginUtils.vipLogin(qqLoginEntity);
        if (result.getCode() == 200){
            Integer id;
            if (name == null){
                String ids = "2551|2514|2516|2493|2494|2464|2465|2428|2427|2426|2351|2319|2320|2321|2232|2239|2240|2276|2275|2274|2273|2272|2271";
                String[] arr = ids.split("\\|");
                id = Integer.parseInt(arr[(int) (Math.random() * arr.length)]);
            }else id = getBubbleId(qqLoginEntity, result.getData(), name);
            if (id != null){
                JSONObject jsonObject = OkHttpUtils.getJsonp("https://g.vip.qq.com/bubble/bubbleSetup?id=" + id + "&platformId=2&uin=" + qqLoginEntity.getQq() + "&version=8.3.0.4480&diyText=%7B%22diyText%22%3A%22" + text + "%22%7D&format=jsonp&t=" + new Date().getTime() + "&g_tk=" + qqLoginEntity.getGtk() + "&p_tk=" + qqLoginEntity.getPt4Token() + "&callback=jsonp0",
                        OkHttpUtils.addCookie(qqLoginEntity.getCookie(result.getData())));
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
    public String qqSign(QQLoginEntity qqLoginEntity) throws IOException {
        Response response = OkHttpUtils.post("https://ti.qq.com/hybrid-h5/api/json/daily_attendance/SignIn",
                OkHttpUtils.addJson("{\"uin\":\"" + qqLoginEntity.getQq() + "\",\"type\":\"1\",\"sld\":\"\"}"),
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "https://ti.qq.com/signin/public/indexv2.html?_wv=1090532257&_wwv=13"));
        if (response.code() == 200){
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            switch (jsonObject.getInteger("ret")){
                case 0: return "打卡成功！！";
                case -3000: return "打卡失败！！！请更新QQ！！";
                case -200: return "打卡失败！！可能未获得测试资格";
                default: return "打卡失败" + jsonObject.getString("msg");
            }
        }else {
            response.close();
            return "打卡失败！！！请售后再试！！";
        }
    }

    @Override
    public String vipGrowthAdd(QQLoginEntity qqLoginEntity) throws IOException {
        LocalDate now = LocalDate.now();
        String nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.CHINA));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        JSONObject jsonObject = OkHttpUtils.postJson("https://proxy.vac.qq.com/cgi-bin/srfentry.fcgi?ts=" + new Date().getTime() + "&g_tk=" + qqLoginEntity.getGtk(),
                OkHttpUtils.addJson(String.format("{\"13357\":{\"month\":%s,\"pageIndex\":1,\"pageSize\":20,\"sUin\":\"%s\",\"year\":%d}}",
                        now.getMonth().getValue(), qqLoginEntity.getQq(), now.getYear())),
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
    public String publishNotice(QQLoginEntity qqLoginEntity, Long group, String text) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("qid", group.toString());
        map.put("bkn", qqLoginEntity.getGtk());
        map.put("text", text);
        map.put("pinned", "0");
        map.put("type", "1");
        map.put("settings", "{\"is_show_edit_card\":0,\"tip_window_type\":1,\"confirm_required\":0}");
        JSONObject jsonObject = OkHttpUtils.postJson("https://web.qun.qq.com/cgi-bin/announce/add_qun_notice", map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0: return "发公告成功！！";
            case 35: return "我还不是管理员呢，不能发送公告！";
            case 1: return "发送公告失败，请更新QQ！";
            default: return "发公告失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public String getGroupLink(QQLoginEntity qqLoginEntity, Long group) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("gc", group.toString());
        map.put("type", "1");
        map.put("bkn", qqLoginEntity.getGtk());
        JSONObject jsonObject = OkHttpUtils.postJson("https://admin.qun.qq.com/cgi-bin/qun_admin/get_join_link", map,
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), "https://admin.qun.qq.com/create/share/index.html?ptlang=2052&groupUin=" + group));
        switch (jsonObject.getInteger("ec")){
            case 0: return jsonObject.getString("url");
            case 1: return "获取链接失败，请更新QQ！！";
            default: return "加群链接获取失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public String groupActive(QQLoginEntity qqLoginEntity, Long group, Integer page) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://qqweb.qq.com/c/activedata/get_mygroup_data?bkn=%s&gc=%d&page=%d",
                qqLoginEntity.getGtk(), group, page), OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
    public String weiShiSign(QQLoginEntity qqLoginEntity) throws IOException {
        Response response = OkHttpUtils.get("https://h5.qzone.qq.com/weishi/jifen/main?_proxy=1&_wv=3&navstyle=2&titleh=55.0&statush=20.0",
                OkHttpUtils.addHeaders(qqLoginEntity.getCookie(), null, UA.MOBILE));
        String str = OkHttpUtils.getStr(response);
        if (!"错误提示".equals(Jsoup.parse(str).getElementsByTag("title").first().text())){
            String gtk = qqLoginEntity.getGtk();
            StringBuilder sb = new StringBuilder();
            String cookie = OkHttpUtils.getCookie(response);
            cookie += qqLoginEntity.getCookie();
            Map<String, String> map = new HashMap<>();
            map.put("task_appid", "weishi");
            map.put("task_id", "SignIn");
            map.put("qua", "_placeholder");
            map.put("format", "json");
            map.put("uin", qqLoginEntity.getQq().toString());
            map.put("inCharset", "utf-8");
            map.put("outCharset", "utf-8");
            JSONObject jsonObject = OkHttpUtils.postJson(String.format("https://h5.qzone.qq.com/proxy/domain/activity.qzone.qq.com/fcg-bin/fcg_weishi_task_report_login?t=0%s030444&g_tk=%s",
                    new Date().getTime(), gtk), map, OkHttpUtils.addCookie(cookie));
            if (jsonObject.getInteger("code") == 0) sb.append("微视签到成功！！");
            else sb.append("微视签到失败，").append(jsonObject.getString("message"));
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("wesee_fe_map_ext", "{\"deviceInfoHeader\":\"i=undefined\",\"qimei\":\"7e8454fad0148911\",\"imei\":\"\"}");
            headerMap.put("referer", "https://isee.weishi.qq.com/ws/app-pages/task_center/index.html?h5from=center&offlineMode=1&h5_data_report={%22navstyle%22:%222%22,%22needlogin%22:%221%22,%22_wv%22:%224096%22}&titleh=55.0&statush=27.272728");
            headerMap.put("cookie", cookie);
            headerMap.put("user-agent", "V1_AND_WEISHI_6.8.0_590_435013001_D/Mozilla/5.0 (Linux; Android 10; MI 9 Build/QKQ1.190825.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.92 Mobile Safari/537.36 QQJSSDK/1.3");
            jsonObject = OkHttpUtils.postJson("https://api.weishi.qq.com/trpc.weishi.weishi_h5_proxy.weishi_h5_proxy/GetUserTaskList?g_tk=" + gtk,
                    OkHttpUtils.addJson("{\"msg\":\"{\\\"sceneId\\\":1003,\\\"extInfo\\\":{}}\"}"),
                    OkHttpUtils.addHeaders(headerMap));
            if (jsonObject.getInteger("ret") == 0){
                jsonObject = JSON.parseObject(jsonObject.getString("msg"));
                JSONObject taskJsonObject = jsonObject.getJSONObject("taskInfoMp");
                int id = 0;
                for (Map.Entry<String, Object> entry: taskJsonObject.entrySet()){
                    JSONObject singleJsonObject = (JSONObject) entry.getValue();
                    JSONObject taskInfoJsonObject = singleJsonObject.getJSONObject("taskInfoCfg");
                    if ("QQ等级加速".equals(taskInfoJsonObject.getString("taskName"))){
                        id = taskInfoJsonObject.getInteger("taskId");
                        break;
                    }
                }
                JSONObject resultJsonObject = OkHttpUtils.postJson("https://api.weishi.qq.com/trpc.weishi.weishi_h5_proxy.weishi_h5_proxy/ObtainTaskReward?g_tk=" + gtk,
                        OkHttpUtils.addJson("{\"msg\":\"{\\\"taskId\\\":" + id + "}\"}"), OkHttpUtils.addHeaders(headerMap));
                switch (resultJsonObject.getInteger("ret")){
                    case 0: sb.append("微视服务加速成功！成长值+0.5天"); break;
                    case 2007: sb.append("微视服务今天已完成加速！"); break;
                    default: sb.append("微视领取任务奖励失败！").append(resultJsonObject.getString("err_msg"));
                }
            }else {
                sb.append("QQ微视获取任务详情失败！").append(jsonObject.getString("err_msg"));
            }
            return sb.toString();
        }else return "微视签到失败，请更新QQ！";
    }


    private Result<List<Map<String, String>>> getGroupFileList(QQLoginEntity qqLoginEntity, Long group, String folderName, String folderId) throws IOException {
        if (folderId == null) folderId = "%2F";
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://pan.qun.qq.com/cgi-bin/group_file/get_file_list?gc=%d&bkn=%s&start_index=0&cnt=30&filter_code=0&folder_id=%s",
                group, qqLoginEntity.getGtk(), folderId), OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0:
                JSONArray filesJsonArray = jsonObject.getJSONArray("file_list");
                List<Map<String, String>> list = new ArrayList<>();
                String id = null;
                for (Object obj: filesJsonArray){
                    JSONObject fileJsonObject = (JSONObject) obj;
                    if (folderName == null){
                        if (fileJsonObject.getInteger("type") == 1){
                            Map<String, String> map = new HashMap<>();
                            map.put("busId", fileJsonObject.getString("bus_id"));
                            map.put("id", fileJsonObject.getString("id"));
                            map.put("name", fileJsonObject.getString("name"));
                            map.put("parentId", fileJsonObject.getString("parent_id"));
                            list.add(map);
                        }
                    }else {
                        if (fileJsonObject.getInteger("type") == 2) {
                            if (folderName.equals(fileJsonObject.getString("name"))) {
                                id = URLEncoder.encode(fileJsonObject.getString("id"), "utf-8");
                                break;
                            }
                        }
                    }
                }
                if (id != null){
                    return getGroupFileList(qqLoginEntity, group, null, id);
                }
                if (folderName != null) return Result.failure("没有找到该文件夹", null);
                return Result.success(list);
            case -107: return Result.failure("获取群文件失败，您还没有加入该群！！", null);
            case 4:
            case 1: return Result.failure("获取群文件失败，请更新QQ！", null);
            default: return Result.failure("获取群文件失败" + jsonObject.getString("em"), null);
        }
    }

    private String getGroupFileUrl(QQLoginEntity qqLoginEntity, Long group, String busId, String id) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJsonp(String.format("https://pan.qun.qq.com/cgi-bin/group_share_get_downurl?uin=%d&groupid=%d&pa=%s&r=0.%s&charset=utf-8&g_tk=%s&callback=_Callback",
                qqLoginEntity.getQq(), group, URLEncoder.encode("/" + busId + id, "utf-8"), BotUtils.randomNum(16), qqLoginEntity.getGtk()),
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        if (jsonObject.getInteger("code") == 0) return jsonObject.getJSONObject("data").getString("url");
        else return "获取链接失败！！";
    }

    @Override
    public String groupFileUrl(QQLoginEntity qqLoginEntity, Long group, String folderName) throws IOException {
        Result<List<Map<String, String>>> result = getGroupFileList(qqLoginEntity, group, folderName, null);
        if (result.getCode() == 200){
            if (folderName == null) folderName = "/";
            StringBuilder sb = new StringBuilder("本群的目录<" + folderName + ">的群文件如下：\n");
            List<Map<String, String>> list = result.getData();
            for (Map<String, String> map: list){
                String url = getGroupFileUrl(qqLoginEntity, group, map.get("busId"), map.get("id"));
                sb.append("文件名：").append(map.get("name")).append("\n");
                sb.append("链接：").append(BotUtils.shortUrl(url + "/" + URLEncoder.encode(map.get("name"), "utf-8"))).append("\n");
                sb.append("--------------\n");
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        }else return result.getMessage();
    }

    @Override
    public String allShutUp(QQLoginEntity qqLoginEntity, Long group, Boolean isShutUp) throws IOException {
        long num = 0L;
        if (isShutUp) num = 4294967295L;
        Map<String, String> map = new HashMap<>();
        map.put("src", "qinfo_v3");
        map.put("gc", group.toString());
        map.put("bkn", qqLoginEntity.getGtk());
        map.put("all_shutup", String.valueOf(num));
        JSONObject jsonObject = OkHttpUtils.postJson("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_shutup", map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
    public String changeName(QQLoginEntity qqLoginEntity, Long qq, Long group, String name) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("u", qq.toString());
        map.put("name", name);
        map.put("gc", group.toString());
        map.put("bkn", qqLoginEntity.getGtk());
        map.put("src", "qinfo_v3");
        JSONObject jsonObject = OkHttpUtils.postJson("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_card", map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
    public String setGroupAdmin(QQLoginEntity qqLoginEntity, Long qq, Long group, Boolean isAdmin) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("u", qq.toString());
        int op = 0;
        if (isAdmin) op = 1;
        map.put("op", String.valueOf(op));
        map.put("gc", group.toString());
        map.put("bkn", qqLoginEntity.getGtk());
        map.put("src", "qinfo_v3");
        JSONObject jsonObject = OkHttpUtils.postJson("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_admin", map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
    public String growthLike(QQLoginEntity qqLoginEntity) throws IOException {
        Result<String> result = QQSuperLoginUtils.vipLogin(qqLoginEntity);
        if (result.getCode() == 200){
            String psKey = result.getData();
            String url = String.format("https://mq.vip.qq.com/m/growth/rank?ADTAG=vipcenter&_wvSb=1&traceNum=2&traceId=%s%s",
                    qqLoginEntity.getQq(), String.valueOf(new Date().getTime()).substring(0, 11));
            Response response = OkHttpUtils.get(url, OkHttpUtils.addCookie(qqLoginEntity.getCookie(psKey)));
            if (response.code() == 200){
                String html = OkHttpUtils.getStr(response);
                Elements elements = Jsoup.parse(html).getElementsByClass("f-uin");
                for (Element ele: elements){
                    String toUin = ele.text();
                    if (qqLoginEntity.getQq().toString().equals(toUin)) continue;
                    JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://mq.vip.qq.com/m/growth/doPraise?method=0&toUin=%s&g_tk=%s&ps_tk=%s",
                            toUin, QQUtils.getGTK2(qqLoginEntity.getSKey()), qqLoginEntity.getGtk(psKey)),
                            OkHttpUtils.addHeaders(qqLoginEntity.getCookie(psKey), url));
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
    public Result<List<GroupMember>> groupMemberInfo(QQLoginEntity qqLoginEntity, Long group) throws IOException {
        JSONObject jsonObject = OkHttpUtils.getJson(String.format("https://qinfo.clt.qq.com/cgi-bin/qun_info/get_members_info_v1?friends=1&gc=%s&bkn=%s&src=qinfo_v3&_ti=%s",
                group, qqLoginEntity.getGtk(), new Date().getTime()));
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
    public String changePhoneOnline(QQLoginEntity qqLoginEntity, String iMei, String phone) throws IOException {
        Result<String> result = QQSuperLoginUtils.vipLogin(qqLoginEntity);
        if (result.getCode() == 200){
            String psKey = result.getData();
            JSONObject jsonObject = OkHttpUtils.getJson("https://proxy.vip.qq.com/cgi-bin/srfentry.fcgi?ts=" + new Date().getTime() + "&daid=18&g_tk=" + qqLoginEntity.getGtk(psKey) + "&data=%7B%2213031%22:%7B%22req%22:%7B%22sModel%22:%22" + phone + "%22,%22sManu%22:%22vivo%22,%22sIMei%22:%22" + iMei + "%22,%22iAppType%22:3,%22sVer%22:%228.4.1.4680%22,%22lUin%22:" + qqLoginEntity.getQq() + ",%22bShowInfo%22:true,%22sDesc%22:%22%22,%22sModelShow%22:%22" + phone + "%22%7D%7D%7D&pt4_token=" + qqLoginEntity.getPt4Token(),
                    OkHttpUtils.addCookie(qqLoginEntity.getCookie(psKey)));
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

    private String removeGroupFile(QQLoginEntity qqLoginEntity, Long group, String busId, String id, String parentId) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("src", "qpan");
        map.put("gc", group.toString());
        map.put("bkn", qqLoginEntity.getGtk());
        map.put("bus_id", busId);
        map.put("file_id", id);
        map.put("app_id", "4");
        map.put("parent_folder_id", parentId);
        map.put("file_list", "{\"file_list\":[{\"gc\":" + group + ",\"app_id\":4,\"bus_id\":" + busId + ",\"file_id\":\"" + id + "\",\"parent_folder_id\":\"" + parentId + "\"}]}");
        JSONObject jsonObject = OkHttpUtils.postJson("https://pan.qun.qq.com/cgi-bin/group_file/delete_file", map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0: return "删除群文件成功！！";
            case 4: return "删除群文件失败，请更新QQ！！";
            case -121: return "权限不够，删除失败！！";
            default: return jsonObject.getString("em");
        }
    }

    @Override
    public String removeGroupFile(QQLoginEntity qqLoginEntity, Long group, String fileName, String folderName) throws IOException {
        Result<List<Map<String, String>>> result = getGroupFileList(qqLoginEntity, group, folderName, null);
        List<Map<String, String>> list = result.getData();
        if (list == null) return result.getMessage();
        boolean status = false;
        for (Map<String, String> map: list){
            if (map.get("name").contains(fileName)){
                status = true;
                String rmResult = removeGroupFile(qqLoginEntity, group, map.get("busId"), map.get("id"), map.get("parentId"));
                if (rmResult.contains("失败")) return rmResult;
            }
        }
        if (status) return "删除群文件成功！！";
        else return "没有找到该文件";
    }

    @Override
    public String queryFriendVip(QQLoginEntity qqLoginEntity, Long qq, String psKey) throws IOException {
        if (psKey == null) {
            Result<String> result = QQSuperLoginUtils.vipLogin(qqLoginEntity);
            if (result.getCode() == 200) psKey = result.getData();
            else return result.getMessage();
        }
        String html = OkHttpUtils.getStr("https://h5.vip.qq.com/p/mc/privilegelist/other?friend=" + qq,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie(psKey)));
        Elements elements = Jsoup.parse(html).select(".guest .grade .icon-level span");
        StringBuilder sb = new StringBuilder().append("qq（").append(qq).append("）的开通业务如下：");
        for (Element ele: elements){
            sb.append(ele.text()).append("\n");
        }
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    @Override
    public String queryLevel(QQLoginEntity qqLoginEntity, Long qq, String psKey) throws IOException {
        if (psKey == null){
            Result<String> result = QQSuperLoginUtils.vipLogin(qqLoginEntity);
            if (result.getCode() == 200) psKey = result.getData();
            else return result.getMessage();
        }
        Response response = OkHttpUtils.get("https://club.vip.qq.com/api/vip/getQQLevelInfo?requestBody=%7B%22sClientIp%22:%22127.0.0.1%22,%22sSessionKey%22:%22" + qqLoginEntity.getSKey() + "%22,%22iKeyType%22:1,%22iAppId%22:0,%22iUin%22:" + qqLoginEntity.getQq() + "%7D",
                OkHttpUtils.addCookie(qqLoginEntity.getCookie(psKey)));
        if (response.code() == 200){
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            return jsonObject.getJSONObject("data").getJSONObject("mRes").getString("iQQLevel");
        }else return "获取等级失败，请更新QQ！！";
    }

    @Override
    public List<Map<String, String>> getGroupMsgList(QQLoginEntity qqLoginEntity) throws IOException {
        String html = OkHttpUtils.getStr("https://web.qun.qq.com/cgi-bin/sys_msg/getmsg?ver=5761&filter=0&ep=0",
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
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
    public String operatingGroupMsg(QQLoginEntity qqLoginEntity, String type, Map<String, String> map, String refuseMsg) throws IOException {
        String url = "https://web.qun.qq.com/cgi-bin/sys_msg/set_msgstate";
        Headers cookie = OkHttpUtils.addCookie(qqLoginEntity.getCookie());
        FormBody.Builder builder = new FormBody.Builder()
                .add("seq", map.get("seq"))
                .add("t", map.get("typeInt"))
                .add("gc", map.get("group"))
                .add("uin", qqLoginEntity.getQq().toString())
                .add("ver", "false")
                .add("from", "2")
                .add("bkn", qqLoginEntity.getGtk());
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
}
