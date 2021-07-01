package me.kuku.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import com.alibaba.fastjson.JSONObject;
import me.kuku.pojo.Result;
import me.kuku.yuq.entity.BiliBiliEntity;
import me.kuku.yuq.pojo.BiliBiliPojo;
import okio.ByteString;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
@AutoBind
public interface BiliBiliLogic {
    Result<List<BiliBiliPojo>> getIdByName(String username) throws IOException;
    String convertStr(BiliBiliPojo biliBiliPojo);
    Result<List<BiliBiliPojo>> getDynamicById(String id) throws IOException;
    List<BiliBiliPojo> getAllDynamicById(String id) throws IOException;
    String loginByQr1() throws IOException;
    Result<BiliBiliEntity> loginByQr2(String url) throws IOException;
    Result<BiliBiliEntity> loginByPassword(String username, String password) throws IOException;
    Result<List<BiliBiliPojo>> getFriendDynamic(BiliBiliEntity biliBiliEntity) throws IOException;
    JSONObject live(String id) throws IOException;
    String liveSign(BiliBiliEntity biliBiliEntity) throws IOException;
    String like(BiliBiliEntity biliBiliEntity, String id, Boolean isLike) throws IOException;
    String comment(BiliBiliEntity biliBiliEntity, String rid, String type, String content) throws IOException;
    String forward(BiliBiliEntity biliBiliEntity, String id, String content) throws IOException;
    String tossCoin(BiliBiliEntity biliBiliEntity, String rid, int count) throws IOException;
    String favorites(BiliBiliEntity biliBiliEntity, String rid, String name) throws IOException;
    Result<?> uploadImage(BiliBiliEntity biliBiliEntity, ByteString byteString) throws IOException;
    String publishDynamic(BiliBiliEntity biliBiliEntity, String content, List<String> images) throws IOException;
    List<Map<String, String>> getRanking() throws IOException;
    String report(BiliBiliEntity biliBiliEntity, String aid, String cid, int proGRes) throws IOException;
    String share(BiliBiliEntity biliBiliEntity, String aid) throws IOException;
    List<Map<String, String>> getReplay(BiliBiliEntity biliBiliEntity, String oid, int page) throws IOException;
    String reportComment(BiliBiliEntity biliBiliEntity, String oid, String rpId, int reason) throws IOException;
    String getOidByBvId(String bvId) throws IOException;
}
