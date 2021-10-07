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

@AutoBind
public interface BiliBiliLogic {
	Result<List<BiliBiliPojo>> getIdByName(String username) throws IOException;
	String convertStr(BiliBiliPojo biliBiliPojo);
	Result<List<BiliBiliPojo>> getDynamicById(String id) throws IOException;
	List<BiliBiliPojo> getAllDynamicById(String id) throws IOException;
	String loginByQr1() throws IOException;
	Result<BiliBiliEntity> loginByQr2(String url) throws IOException;
	Result<List<BiliBiliPojo>> getFriendDynamic(BiliBiliEntity biliBiliEntity) throws IOException;
	JSONObject live(String id) throws IOException;
	Result<?> uploadImage(BiliBiliEntity biliBiliEntity, ByteString byteString) throws IOException;
	List<Map<String, String>> getRanking() throws IOException;
	List<Map<String, String>> getReplay(BiliBiliEntity biliBiliEntity, String oid, int page) throws IOException;
	String reportComment(BiliBiliEntity biliBiliEntity, String oid, String rpId, int reason) throws IOException;
	String getOidByBvId(String bvId) throws IOException;
	Result<List<Map<String, String>>> followed(BiliBiliEntity biliBiliEntity) throws IOException;
}
