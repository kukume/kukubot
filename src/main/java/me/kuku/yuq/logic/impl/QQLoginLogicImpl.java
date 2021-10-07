package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.utils.OkHttpUtils;
import me.kuku.yuq.entity.QqLoginEntity;
import me.kuku.yuq.logic.QqLoginLogic;
import me.kuku.yuq.pojo.GroupMember;
import okhttp3.MultipartBody;

import java.io.IOException;
import java.util.*;

public class QQLoginLogicImpl implements QqLoginLogic {

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

}
