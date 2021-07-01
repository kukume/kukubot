package me.kuku.yuq.logic.impl;

import com.baidu.aip.contentcensor.AipContentCensor;
import com.baidu.aip.ocr.AipOcr;
import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import me.kuku.pojo.Result;
import me.kuku.utils.OkHttpUtils;
import me.kuku.yuq.entity.ConfigEntity;
import me.kuku.yuq.logic.AILogic;
import me.kuku.yuq.pojo.BaiduAIPojo;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.utils.BotUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@SuppressWarnings({"SameParameterValue", "unused"})
@Named("baiduAILogic")
public class BaiduAILogicImpl implements AILogic {

	@Inject
	private ConfigService configService;

	private BaiduAIPojo getByConfig(String type){
		ConfigEntity configEntity = configService.findByType("baiduAI");
		com.alibaba.fastjson.JSONObject jsonObject = configEntity.getContentJsonObject().getJSONObject(type);
		String appId = jsonObject.getString("appId");
		String appKey = jsonObject.getString("appKey");
		String secretKey = jsonObject.getString("secretKey");
		return new BaiduAIPojo(appId, appKey, secretKey);
	}

	private AipOcr getAipOcr(){
		BaiduAIPojo baiduAIPojo = getByConfig("baiduAIOcr");
		return new AipOcr(baiduAIPojo.getAppId(), baiduAIPojo.getAppKey(), baiduAIPojo.getSecretKey());
	}

	private AipContentCensor getAipContentCensor(){
		BaiduAIPojo baiduAIPojo = getByConfig("baiduAIContentCensor");
		return new AipContentCensor(baiduAIPojo.getAppId(),
				baiduAIPojo.getAppKey(), baiduAIPojo.getSecretKey());
	}

	private AipSpeech getAipSpeech(){
		BaiduAIPojo baiduAIPojo = getByConfig("baiduAISpeech");
		return new AipSpeech(baiduAIPojo.getAppId(), baiduAIPojo.getAppKey(),
				baiduAIPojo.getSecretKey());
	}

	@Override
	public boolean pornIdentification(String imageUrl) throws IOException {
		JSONObject jsonObject = getAipContentCensor().imageCensorUserDefined(OkHttpUtils.getBytes(imageUrl), null);
		if (!jsonObject.has("error_code")) {
			if (jsonObject.getInt("conclusionType") == 1) return false;
			if (jsonObject.has("data")) {
				JSONArray jsonArray = jsonObject.getJSONArray("data");
				if (jsonArray.length() == 0) return false;
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject singJsonObject = jsonArray.getJSONObject(i);
					if (singJsonObject.getInt("type") == 1) return true;
				}
			}else return false;
		}
		return false;
	}

	@Override
	public String generalOCR(String imageUrl) throws IOException {
		JSONObject jsonObject = getAipOcr().basicGeneral(OkHttpUtils.getBytes(imageUrl), null);
		if (jsonObject.has("error_code")){
			return jsonObject.getString("error_msg");
		}else {
			if (jsonObject.getInt("words_result_num") == 0) return "没有识别到任何结果！！";
			JSONArray jsonArray = jsonObject.getJSONArray("words_result");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < jsonArray.length(); i++) {
				sb.append(jsonArray.getJSONObject(i).getString("words")).append("\n");
			}
			return BotUtils.removeLastLine(sb);
		}
	}

	@Override
	public Result<byte[]> voiceSynthesis(String text) throws IOException {
		TtsResponse result = getAipSpeech().synthesis(text, "zh", 1, null);
		JSONObject jsonObject = result.getResult();
		if (jsonObject != null){
			return Result.failure(jsonObject.getString("error_msg"), null);
		}else return Result.success(result.getData());
	}

	@Override
	public String voiceIdentify(String voiceUrl) throws IOException {
		JSONObject jsonObject = getAipSpeech().asr(OkHttpUtils.getBytes(voiceUrl), "wav", 8000, null);
		if (jsonObject.getInt("err_no") != 0){
			return jsonObject.getString("err_msg");
		}else {
			return jsonObject.getJSONArray("result").getString(0);
		}
	}
}
