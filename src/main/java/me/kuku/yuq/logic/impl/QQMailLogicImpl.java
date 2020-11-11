package me.kuku.yuq.logic.impl;

import me.kuku.yuq.entity.QQLoginEntity;
import me.kuku.yuq.logic.QQMailLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.FormBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QQMailLogicImpl implements QQMailLogic {

    Result<Map<String, String>> login(QQLoginEntity qqLoginEntity){
        return Result.success(new HashMap<>());
    }
    /*
    fun login(qqLoginEntity: QQLoginEntity): CommonResult<Map<String, String>> {
        val commonResult = QQPasswordLoginUtils.login("522005705", "4", qqLoginEntity.qq.toString(), qqLoginEntity.password, "https://mail.qq.com/cgi-bin/readtemplate?check=false&t=loginpage_new_jump&vt=passport&vm=wpt&ft=loginpage&target=")
        return if (commonResult.code == 200){
            val psKey = commonResult.t!!.getValue("p_skey")
            val response = OkHttpClientUtils.get("http://mail.qq.com/cgi-bin/login?vt=passport&vm=wpt&ft=loginpage&target=",
                    OkHttpClientUtils.addCookie(qqLoginEntity.getCookie(psKey)))
            val cookie = OkHttpClientUtils.getCookie(response)
            val html = OkHttpClientUtils.getStr(response)
            val sid = BotUtils.regex("sid\\=", "\"", html)
            CommonResult(200, "成功", mapOf("cookie" to cookie, "sid" to sid!!))
        }else commonResult
    }
     */

    @Override
    public Result<List<Map<String, String>>> getFile(QQLoginEntity qqLoginEntity) throws IOException {
        Result<Map<String, String>> result = login(qqLoginEntity);
        if (result.getCode() == 200){
            Map<String, String> map = result.getData();
            String resultStr = OkHttpUtils.getStr("https://mail.qq.com/cgi-bin/ftnExs_files?sid=" + map.get("sid") + "&t=ftn.json&s=list&ef=js&listtype=self&up=down&sorttype=createtime&page=0&pagesize=50&pagemode=more&pagecount=2&ftnpreload=true&sid=" + map.get("sid"),
                    OkHttpUtils.addCookie(map.get("cookie")));
            List<Map<String, String>> list = new ArrayList<>();
            Matcher sId = Pattern.compile("(?<=sFileId : \").+?(?=\")").matcher(resultStr);
            Matcher sName = Pattern.compile("(?<=sName : \").+?(?=\")").matcher(resultStr);
            Matcher sKey = Pattern.compile("(?<=sKey : \").+?(?=\")").matcher(resultStr);
            Matcher sCode = Pattern.compile("(?<=sFetchCode : \").+?(?=\")").matcher(resultStr);
            while (sId.find() && sName.find() && sKey.find() && sCode.find()){
                Map<String, String> fileMap = new HashMap<>();
                fileMap.put("fileId", sId.group());
                fileMap.put("sName", sName.group());
                fileMap.put("sKey", sKey.group());
                fileMap.put("sCode", sCode.group());
                fileMap.putAll(map);
                list.add(fileMap);
            }
            return Result.success(list);
        }else return Result.failure(result.getMessage(), null);
    }

    @Override
    public String fileRenew(QQLoginEntity qqLoginEntity) throws IOException {
        Result<List<Map<String, String>>> result = getFile(qqLoginEntity);
        if (result.getCode() == 200){
            FormBody.Builder builder = new FormBody.Builder();
            List<Map<String, String>> list = result.getData();
            if (list.size() != 0){
                String sid = list.get(0).get("sid");
                String cookie = list.get(0).get("cookie");
                for (Map<String, String> map: list){
                    builder.add("fid", map.get("fileId"));
                }
                OkHttpUtils.post("https://mail.qq.com/cgi-bin/ftnExtendfile?sid=" + sid + "&t=ftn.json&s=oper&ef=js&keytext=&sid=" + sid,
                        builder.build(), OkHttpUtils.addCookie(cookie)).close();
                return "QQ邮箱中转站续期成功！共续期了" + list.size() + "个文件";
            }else return "您的QQ邮箱中转站还没有文件呢！";
        }else return result.getMessage();
    }
}
