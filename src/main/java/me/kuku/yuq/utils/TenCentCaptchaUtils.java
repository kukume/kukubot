package me.kuku.yuq.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.UA;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TenCentCaptchaUtils {

    private final static String ua = "TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzgzLjAuNDEwMy4xMTYgU2FmYXJpLzUzNy4zNg==";

    private static String getCaptchaPictureUrl(String appId, String qq, String imageId, String sig, String sess, String sid, int index){
        return String.format("https://t.captcha.qq.com/hycdn?index=%s&image=%s?aid=%s&captype=&curenv=inner&protocol=https&clientype=1&disturblevel=&apptype=2&noheader=0&color=&showtype=&fb=1&theme=&lang=2052&ua=%s&enableDarkMode=0&grayscale=1&subsid=3&sess=%s&fwidth=0&sid=%s&forcestyle=undefined&wxLang=&tcScale=1&uid=%s&cap_cd=%s&rnd=%s&TCapIframeLoadTime=60&prehandleLoadTime=135&createIframeStart=%s487&rand=%s&websig=&vsig=&img_index=%s",
                index, imageId, appId, ua, sess, sid, qq, sig,  BotUtils.randomNum(6), new Date().getTime(), BotUtils.randomNum(6), index);
    }

    private static Map<String, String> getCollect(int width, String sid) throws IOException {
        String token = BotUtils.randomLong(2067831491, 5632894513L).toString();
        int sx = BotUtils.randomInt(700, 730);
        int sy = BotUtils.randomInt(295, 300);
        int ex = sx + (width -55) / 2;
        int sTime = BotUtils.randomInt(100, 300);
        StringBuilder res = new StringBuilder("[" + sx + "," + sy + "," + sTime + "],");
        int[] randy = new int[]{0,0,0,0,0,0,1,1,1,2,3,-1,-1,-1,-2};
        while (sx < ex){
            int x = BotUtils.randomInt(3, 9);
            sx += x;
            int y = randy[(int) (Math.random() * (randy.length - 1))];
            int time = BotUtils.randomInt(9, 18);
            sTime += time;
            res.append("[").append(x).append(",").append(y).append(",").append(time).append("],");
        }
        res.append("[0,0,").append(BotUtils.randomInt(10, 25)).append("]");
        String js = OkHttpUtils.getStr("https://t.captcha.qq.com/tdc.js?app_data=" + sid + "&t=" + new Date().getTime());
        String base64Str = Base64.getEncoder().encodeToString(js.getBytes());
        Map<String, String> map = new HashMap<>();
        map.put("script", base64Str);
        map.put("tokenid", token);
        map.put("slideValue", res.toString());
        Response response = OkHttpUtils.post("http://collect.qqzzz.net", map,
                OkHttpUtils.addUA("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36"));
        String collectData;
        String eks;
        String length;
        if (response.code() == 200){
            JSONObject jsonObject = OkHttpUtils.getJson(response);
            collectData = URLDecoder.decode(jsonObject.getString("collectdata"), "utf-8");
            eks = jsonObject.getString("eks");
            length = String.valueOf(collectData.length());
        }else {
            collectData = "nIXLBDdHYhKzs/DpuAKh/q/xncVi259oy lhhF6ZZj NNILYRaA/ddVcg7XNV3vCVTrbqZmE9W oFhcOKrFqRFsrfocYSan2U EVz2etrit89QbKQcLm8SI/Z9meICCwYeiZkPo8OBf7RhLvsJaRVL7asZTh9CYuFI4PpiEcNzArOdoM2QtOHXBboMfQEqItUbqI4YseKYQKgEv0pjL9GUh8KU3Da2qjhArZWm5CBjzSn9hLqlPcBSxpOfzcpAos7DIV/cNZjqNewnobJsc3miRgiKant7B1sI6EKgFEgvdyWzpHL6NmPqPHHRLkp22USYBV3veSrmAHp hvhs1bUnovmFgsdTtcksyc8zCETiE9BKRR0nAWRd8oWOfW2lau7yEsba70gD3aKR/yL6fMxI6dQn2/lmgIl5TQYGWSBzNfaHGG7vwdE1U//9HO zVSKSiUXWmQTJP2FjZLQSUia6/xncVi259oFI4PpiEcNzDMwXM1gr2hoYeOYVEIUmcjapH56k9ykKZDMy7 IoDBMhDxhW6yAmgqU2uPrKq mNxr6NzjoqHW26/xncVi259o xLbqRK95Ikxtlii7eY0A8vvyY6u4DK9bfx4Gy9vG5tUMHU1eiRo8yBK5WKBYlFOKWYOMxJZSraq0hEM7ZurpaH0xi fZ8BLvcoO9RCbMR1KmQINEnrix//AzurahzS9jARAshgPILp bkOjnkfhuoJqdU5Y90g/AqniuToChF5vJC0r8BEhlm1ix88Xaxi4AY9hBCCo8Sc5A  VloOosyxpOfzcpAos C30FEfypkeyEIN5HVFbvMlVAezZaZhOMDLOYUWulr6GpwWmhGYMJK/xncVi259ob9G0HSGpInrL2O/5B3Yrc35eW2eKVr2dGIh7lG8FSce wdLUSsynjcjWj35 VDOl2E2NKeewBU7Rqlp5NAcBQPvAH245/lhAUr8 BkbjSlU7IzdBUlTepfZ2pIPeoUSslwbf1FfKmR6oAUlm6WBeZ4Tn5l2A0F7HTVm8mns pfjv1y2BlRf0mp68/WsFx9VD6RSs4iw1hL83jrMgxlCUwBDWSLdjMv8o2db8wsrRwrVsbmsp4kXYc4Tn5l2A0F7HQtO2/095TWUvU/ig26uXqBsoXRVivN Iw1cg5jrO/ ho2JLG0qeQzY7NwUfo5HuWBtyIXnjb78MOIE7tVxMQfFW7X /XltgehOfmXYDQXsdquBPxb3G53O/XLYGVF/SaRuJKob592HJHUJsseZ4TquCJuHPF9lUWjDhf/3putkSnlJt8VM 8dlYwNLjm/EasudZWFzWNuUtu47gcvUcMxcjWj35 VDOlZCtQnImNPqceOFouTvK/GTWJtKANPYFVw1cg5jrO/ juxeQWYnGmoM/yzIAh6dHCudZWFzWNuUteV61qeHu8zQGZ8JZh9XQYiGHzoWU9t0HI1o9 flQzpfGFFQaNDxzYZCtQnImNPqcvU/ig26uXqBsoXRVivN I8f1p7ijswxUxdPahSfcpJow4X/96brZEDhRTuNYTtGbsXk1/pxpdoFW7X /Xltgecjajn3lGQKHI1o9 flQzpWQrUJyJjT6nL1P4oNurl6hG4kqhvn3YchK6vPZFTfkZTh1dstLeIB3uxeQWYnGmoHTxYtD2lQrAX7HOMsSAXilWgpqdCVySFRxPM/C9ICFuyNaPfn5UM6VkK1CciY0 py9T KDbq5eoRuJKob592HISurz2RU35GU4dXbLS3iAdbR84SHIhvp3uxeQWYnGmoAafJWm4kt2h1WaDuPW4A1cBmfCWYfV0GFW7X /XltgeyNaPfn5UM6WK1tDDEOzW8Q2KNavPBklYymItofZtRSQq0P6zBpcKjztbblAdkucTeA6uOgWZoh/mFLoweZz1ZYyQQ0D5Pe7BBp8labiS3aFfsc4yxIBeKWA3bbheW1Zw/fqyFa8aDUadIBnWivxtAGQrUJyJjT6nymItofZtRSSZLQ 1Z4IRG0biSqG fdhyErq89kVN RnmFLoweZz1Ze7F5BZicaag2Um2K/ JlTjVZoO49bgDVw3Z5 Ht0OvfdqIaaJNBEDtj9M07Ucycy2QrUJyJjT6nymItofZtRSRlKrjENjo5rM jm/ooPHNdud6pxLUnw2ZtHzhIciG nW0fOEhyIb6dtlXYHALckMYNZOMI8XUwovy8c/EF/ZVLX7HOMsSAXinBYr r5LQdi/CiK5JMQMgeajgP8oNlPdc6tJC0/SNqPK8IBVeQjebewA8XNwcz9KegdrqbQDYzzTsjN0FSVN6lDWTjCPF1MKLZSbYr/4mVONjB/2Sofv0dfhQQ 4bwJ71fsc4yxIBeKQ3Z5 Ht0OvfY/TNO1HMnMtqOA/yg2U91y3COtRGyl7uLcI61EbKXu6ZLQ 1Z4IRG70f2gIDmGFZ5PX6/XMW1L4g8DofrsPo  MJZGO6voSh/Fh/0NrR4MvVZoO49bgDV0Dm1B39ep5SDdnn4e3Q69 dIBnWivxtADO11nFRqMuSLcI61EbKXu6ZLQ 1Z4IRG4KyPpYbFQE7ud6pxLUnw2bqO mAcc2gKdlJtiv/iZU4fhQQ 4bwJ73Ywf9kqH79HX4UEPuG8Ce91WaDuPW4A1cN2efh7dDr32P0zTtRzJzLDp3/OPM3UDtkK1CciY0 pxynVbXN4gQUcMkSZ6usdMwtNN1ZkOA11ReCMIy9GOk4ud6pxLUnw2bNTxIj0qjesOMJZGO6voShlblrTfk7eLFfsc4yxIBeKd5tzOIuCTQrnuGPLklRYbDI1o9 flQzpTO11nFRqMuSDtD043HUMpl3PBKyHwBxTQ25Q7RWKUBG1y0QXy51bOQNZOMI8XUwonF2SLSzVZyGX7HOMsSAXiklQk6QoR7Uc7rDoT6PBufWE3rDuzAbVI7f6qxotQfIvTSVY2so00rUOTzdA/c2YmU2IyUt/GJV11GAibQEF6An Q/iEw9USJpfsc4yxIBeKZ656vcV0wcnyNaPfn5UM6Whfg7ejneEOHDJEmerrHTMPmLGZ2tRB9653qnEtSfDZuX1GpeUPA9PkgOtc5i3VTZcOPL06Le0tCFnH1mpK /DszxgXIsaMRWLVUhHdjj148APFzcHM/Sn31dd/i5NMOHXLRBfLnVs5M66tkZFyOuxX7HOMsSAXikC9ftNMYQQtxdUOakhfgmE7AFqo1s1NP2Soz8C7JP2qY2oTEO0eXZZWlaKOw/VgngfoTXqR7nyqaz/w/JydWcdmrgzsNeRCV2JpGdE VoXsGdNLdS/ipWV6lyWrsBPsqnuEPscFXUaIKCLQdebWOlbFLRU28NAqoljVZzeRMJHTihw 3VPhI2oD81Lg0lnLny/JzN3G4mua4rbZPKzpi8PURwI6zjQYOav8Z3FYtufaK/xncVi259oD1fXd/sGWEyap2GmBZbNST0XE4Sy5oj7vPXABjrvl YtpQp1E38EbamrJHk6bzxyZzI7iJhLPoO14nlDvP6stKpILUbd2kzNYY94QeuIEk5jXUFMjkMagWPSzQxr0bxRQyGj53KvJO81YY1Wt4auWOK71o1lZsv6I2/YgkynBKeEssai 8EFGAlvUZbG5ApgAJSUmkzVvoQAlJSaTNW hAabOrL3pxOzAJSUmkzVvoTNTVUjioOGQEhROMdqlLywBps6svenE7MqSqxbUnqDHVvwmm0dqMQGHvGaX8i/EgzKdtJZsOgZOFsr9LTmvyrOWCd2Nxp/S8hXUBp20JsksgjdkzRP99sRiBnpOHE1Bmc4NhDyXddu4wRgUXNh7FeM9/naZHOWr4xCngEpRC830Dg2EPJd127jtK7AnceQVqceqmY/u0Ur9dErq8QdR MN0SurxB1H4w3RK6vEHUfjDXXGajcmulzLLs6sg6MqRx11xmo3Jrpcy/a4jDumertH55f  ZvVIHgeqmY/u0Ur9dErq8QdR MNp2UEXT2giPceqmY/u0Ur9XXGajcmulzLcYE5X5yT3yb2uIw7pnq7Rx6qZj 7RSv1kq/5BOMXRMX2uIw7pnq7R eX/vmb1SB4HqpmP7tFK/Xnl/75m9UgeB6qZj 7RSv1HqpmP7tFK/V1xmo3Jrpcy eX/vmb1SB40SurxB1H4w0eqmY/u0Ur9eeX/vmb1SB40SurxB1H4w0eqmY/u0Ur9eeX/vmb1SB455f  ZvVIHgeqmY/u0Ur9YwMeyIZfLJ755f  ZvVIHj2uIw7pnq7R eX/vmb1SB455f  ZvVIHhxgTlfnJPfJueX/vmb1SB49riMO6Z6u0cG3IheeNvvw eX/vmb1SB49riMO6Z6u0dxgTlfnJPfJu7v/b6Fzr0D7u/9voXOvQPu7/2 hc69A4wrTJg0mI53cYE5X5yT3yZxgTlfnJPfJva4jDumertHcYE5X5yT3yZxgTlfnJPfJnGBOV ck98muK5xZl9Ckh/RK6vEHUfjDe7v/b6Fzr0DY66dcoWy4gNsq9rN3dG6Pva4jDumertHwq9iKQ40iiZxO7ivjoDR6qmXWnAy ZjX8t4RlQU6occGWGqoZ3N5OuLjWbi0CUW93UL5i1AOsZkGWGqoZ3N5OvLeEZUFOqHHH1KyCOpB2nTt8YyToWjk4OX1GpeUPA9Pjs3BR jke5Zfsc4yxIBeKVw48vTot7S0Y/TNO1HMnMvf6qxotQfIveYw1EOqjmsTz6Ob ig8c13k9fr9cxbUvjsjN0FSVN6l Q/iEw9USJpfsc4yxIBeKVw48vTot7S0sMzLLcz3EnMqeSJzxzJajHDJEmerrHTMggpx9qQmt3gNuUO0VilARjN7sZbZLkrKX7HOMsSAXimnLTdMzOQWmKfxgV3Y54PqN1pinUwcUtHGR/LOq4KB wqA9ORqjUIQectpkzIuK3JUoLWfEVwGGyFgkD7tQms5";
            eks = "UKFp ryeZvXggx5kqYFMxvFv1zcMpCQpR/tNmmmUNeu7ItF6nMPVbcUXkZ0S bjoElJtNAE5conKhyyrXXcU/Yq8jLa92FDkEmgD/ASlHFjFQI2wnJcTZ83mHayjhmXUpXAxjPaVTZF9ZasX8zLpBzxSayJIAlrb4PAwk6HTIvYBWMwVrfIXDW67ZLqR5cIH9mE1Gjkc49v2yuKvuhZ4Asl4ELSlKP6h27S8c5ZRz3JYl5tBH5Gh5kj6lbf/K9BMHCQDv67q4dtU4kZklOnjxsqy48qT/E7C";
            length = "4928";
        }
        Map<String, String> result = new HashMap<>();
        result.put("collectData", collectData);
        result.put("eks", eks);
        result.put("length", length);
        return result;
    }

    public static int getWidth(String imageAUrl, String imageBUrl) throws IOException {
        BufferedImage imageA = ImageIO.read(new URL(imageAUrl));
        BufferedImage imageB = ImageIO.read(new URL(imageBUrl));
        int imgWidth = imageA.getWidth();
        int imgHeight = imageA.getHeight();
        int t = 0, r = 0;
        for (int i = 0; i < imgHeight - 20; i++){
            for (int j = 0; j < imgWidth - 20; j++){
                int rgbA = imageA.getRGB(j, i);
                int rgbB = imageB.getRGB(j, i);
                if (Math.abs(rgbA - rgbB) > 1800000){
                    t++;
                    r += j;
                }
            }
        }
        return Math.round(Float.parseFloat(String.valueOf(r / t))) -55;
    }

    private static Map<String, String> getCaptcha(String appId, String sig, String qq) throws IOException {
        Response response = OkHttpUtils.get(String.format("https://t.captcha.qq.com/cap_union_prehandle?aid=%s&captype=&curenv=inner&protocol=https&clientype=2&disturblevel=&apptype=2&noheader=&color=&showtype=embed&fb=1&theme=&lang=2052&ua=%s&enableDarkMode=0&grayscale=1&cap_cd=%s&uid=%s&wxLang=&subsid=1&callback=_aq_103418&sess=",
                appId, ua, sig, qq));
        JSONObject jsonObject = JSON.parseObject(BotUtils.regex("\\{.*\\}", OkHttpUtils.getStr(response)));
        String url = String.format("https://t.captcha.qq.com/cap_union_new_show?aid=%s&captype=&curenv=inner&protocol=https&clientype=2&disturblevel=&apptype=2&noheader=&color=&showtype=embed&fb=1&theme=&lang=2052&ua=%s&enableDarkMode=0&grayscale=1&subsid=2&sess=%s&fwidth=0&sid=%s&forcestyle=undefined&wxLang=&tcScale=1&noBorder=noborder&uid=%s&cap_cd=%s&rnd=%s&TCapIframeLoadTime=14&prehandleLoadTime=74&createIframeStart=%s",
                appId, ua, jsonObject.getString("sess"), jsonObject.getString("sid"), qq, sig, BotUtils.randomNum(6), new Date().getTime());
        String html = OkHttpUtils.getStr(url);
        String height = BotUtils.regex("(?<=spt:\\\")(\\d+)(?=\\\")", html);
        String collectName = BotUtils.regex("(?<=collectdata:\\\")([0-9a-zA-Z]+)(?=\\\")", html);
        String sess = BotUtils.regex("sess:\"", "\"", html);
        String imageId = BotUtils.regex("&image=", "\"", html);
        String imageAUrl = getCaptchaPictureUrl(appId, qq, imageId, sig, sess, jsonObject.getString("sid"), 1);
        String imageBUrl = getCaptchaPictureUrl(appId, qq, imageId, sig, sess, jsonObject.getString("sid"), 0);
        int width = getWidth(imageAUrl, imageBUrl);
        String ans = width + "," + height;
        Map<String, String> collect = getCollect(width, jsonObject.getString("sid"));
        Map<String, String> map = new HashMap<>();
        map.put("sess", sess);
        map.put("sid", jsonObject.getString("sid"));
        map.put("qq", qq);
        map.put("sig", sig);
        map.put("ans", ans);
        map.put("collectName", collectName);
        map.put("cdata", "0");
        map.put("width", String.valueOf(width));
        map.put("url", url);
        map.putAll(collect);
        return map;
    }

    private static Result<Map<String, String>> identifyCaptcha(String appId, Map<String, String> map) throws IOException {
        Response response = OkHttpUtils.get("https://ssl.captcha.qq.com/dfpReg?0=Mozilla%2F5.0%20(Windows%20NT%2010.0%3B%20Win64%3B%20x64)%20AppleWebKit%2F537.36%20(KHTML%2C%20like%20Gecko)%20Chrome%2F83.0.4103.116%20Safari%2F537.36&1=zh-CN&2=1.8&3=1.9&4=24&5=8&6=-480&7=1&8=1&9=1&10=u&11=function&12=u&13=Win32&14=0&15=9dcc2da81f0e59e03185ad3db82acb72&16=b845fd62efae6732b958d2b9c29e7145&17=a1835c959081afa32e01bd14786db9b3&18=0&19=76cd47f4e5fcb3f96d6c57addb5498fd&20=824153624864153624&21=1.25%3B&22=1%3B1%3B1%3B1%3B1%3B1%3B1%3B0%3B1%3Bobject0UTF-8&23=0&24=0%3B0&25=31fc8c5fca18c5c1d5acbe2d336b9a63&26=48000_2_1_0_2_explicit_speakers&27=c8205b36aba2b1f3b581d8170984e918&28=ANGLE(AMDRadeon(TM)RXVega10GraphicsDirect3D11vs_5_0ps_5_0)&29=3331cb2359a3c1aded346ac2e279d401&30=a23a38527a38dcea2f63d5d078443f78&31=0&32=0&33=0&34=0&35=0&36=0&37=0&38=0&39=0&40=0&41=0&42=0&43=0&44=0&45=0&46=0&47=0&48=0&49=0&50=0&fesig=15341077811401570658&ut=1066&appid=0&refer=https%3A%2F%2Ft.captcha.qq.com%2Fcap_union_new_show&domain=t.captcha.qq.com&fph=1100805BC31DAAEC6C4B4A686CC8F312CD98DA4D6F7ADB1CE8619BDEB0EA580DE88363A57C65F3A76B2D6D905F49E70266EB&fpv=0.0.15&ptcz=0d372124ed637aa9210ef7ebe9af2ee8e09a1e8919d4b5349e6cf34f21ed2d31&callback=_fp_091990",
                OkHttpUtils.addReferer(map.get("url")));
        String str = OkHttpUtils.getStr(response);
        String fpSig = JSON.parseObject(BotUtils.regex("\\{.*\\}", str)).getString("fpsig");
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("aid", appId);
        paramsMap.put("captype", "");
        paramsMap.put("curenv", "inner");
        paramsMap.put("protocol", "https");
        paramsMap.put("clientype", "2");
        paramsMap.put("disturblevel", "");
        paramsMap.put("apptype", "2");
        paramsMap.put("noheader", "");
        paramsMap.put("color", "");
        paramsMap.put("showtype", "embed");
        paramsMap.put("fb", "1");
        paramsMap.put("theme", "");
        paramsMap.put("lang", "2052");
        paramsMap.put("ua", ua);
        paramsMap.put("enableDarkMode", "0");
        paramsMap.put("grayscale", "1");
        paramsMap.put("subsid", "2");
        paramsMap.put("sess", map.get("sess"));
        paramsMap.put("fwidth", "0");
        paramsMap.put("sid", map.get("sid"));
        paramsMap.put("forcestyle", "undefined");
        paramsMap.put("wxLang", "");
        paramsMap.put("tcScale", "1");
        paramsMap.put("noBorder", "noborder");
        paramsMap.put("uid", map.get("qq"));
        paramsMap.put("cap_cd", map.get("sig"));
        paramsMap.put("rnd", String.valueOf(BotUtils.randomInt(100000, 999999)));
        paramsMap.put("TCapIframeLoadTime", "426");
        paramsMap.put("prehandleLoadTime", "293");
        paramsMap.put("createIframeStart", String.valueOf(new Date().getTime()));
        paramsMap.put("cdata", "0");
        paramsMap.put("ans", map.get("ans"));
        paramsMap.put("vsig", "");
        paramsMap.put("websig", "");
        paramsMap.put("subcapclass", "");
        paramsMap.put(map.get("collectName"), map.get("collectData"));
        paramsMap.put("fpinfo", "fpsig=" + fpSig);
        paramsMap.put("eks", map.get("eks"));
        paramsMap.put("tlg", map.get("length"));
        paramsMap.put("vlg", "0_0_1");
        JSONObject jsonObject = OkHttpUtils.postJson("https://t.captcha.qq.com/cap_union_new_verify", paramsMap,
                OkHttpUtils.addUA(UA.PC));
        if (jsonObject.getInteger("errorCode") == 0){
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("ticket", jsonObject.getString("ticket"));
            resultMap.put("randStr", jsonObject.getString("randstr"));
            return Result.success(resultMap);
        }else return Result.failure(400, "验证码识别失败，请稍后重试！！");
    }

    public static Result<Map<String, String>> identify(String appId, String sig, Long qq) throws IOException {
        Map<String, String> map = getCaptcha(appId, sig, qq.toString());
        return identifyCaptcha(appId, map);
    }

}
