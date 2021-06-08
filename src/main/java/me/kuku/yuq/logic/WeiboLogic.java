package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import me.kuku.yuq.entity.WeiboEntity;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.WeiboPojo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
@AutoBind
public interface WeiboLogic {
    List<String> hotSearch() throws IOException;
    Result<List<WeiboPojo>> getIdByName(String name) throws IOException;
    String convertStr(WeiboPojo weiboPojo);
    Result<List<WeiboPojo>> getWeiboById(String id) throws IOException;
    // 模拟登录
    String getCaptchaUrl(String pcId);
    Result<Map<String, String>> login(Map<String, String> map, String door) throws IOException;
    Result<Map<String, String>> loginByMobile(String username, String password) throws IOException;
    Result<String> loginByMobileSms1(String phone, String cookie) throws IOException;
    Result<WeiboEntity> loginByMobileSms2(String code, String cookie) throws IOException;
    Result<String> loginByMobilePrivateMsg1(String cookie) throws IOException;
    Result<WeiboEntity> loginByMobilePrivateMsg2(String code, String cookie) throws IOException;
    // 电脑端模拟登录已凉
    WeiboEntity loginSuccess(String cookie, String referer, String url) throws IOException;
    Result<Map<String, String>> preparedLogin(String username, String password) throws IOException;
    Result<Map<String, String>> loginBySms1(String token) throws IOException;
    Result<WeiboEntity> loginBySms2(String token, String phone, String code) throws IOException;
    Result<WeiboEntity> loginByPrivateMsg(String token) throws IOException;
    // 电脑端模拟登录已凉
    Map<String, String> loginByQr1() throws IOException;
    Result<WeiboEntity> loginByQr2(String id) throws IOException;
    // 模拟登录
    Result<List<WeiboPojo>> getFriendWeibo(WeiboEntity weiboEntity) throws IOException;
    Result<List<WeiboPojo>> getMyWeibo(WeiboEntity weiboEntity) throws IOException;
    Result<List<WeiboPojo>> weiboTopic(String keyword) throws IOException;
    String like(WeiboEntity weiboEntity, String id) throws IOException;
    String comment(WeiboEntity weiboEntity, String id, String commentContent) throws IOException;
    String forward(WeiboEntity weiboEntity, String id, String content, String picUrl) throws IOException;
    String getUserInfo(String id) throws IOException;
    String publishWeibo(WeiboEntity weiboEntity, String content, List<String> url) throws IOException;
    String removeWeibo(WeiboEntity weiboEntity, String id) throws IOException;
    String favoritesWeibo(WeiboEntity weiboEntity, String id) throws IOException;
    String shortUrl(WeiboEntity weiboEntity, String url) throws IOException;
}
