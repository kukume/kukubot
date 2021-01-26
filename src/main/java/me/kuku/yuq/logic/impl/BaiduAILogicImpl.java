package me.kuku.yuq.logic.impl;

import com.IceCreamQAQ.Yu.annotation.HookBy;
import com.baidu.aip.contentcensor.AipContentCensor;
import com.baidu.aip.contentcensor.EImgType;
import com.baidu.aip.ocr.AipOcr;
import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import me.kuku.yuq.logic.AILogic;
import me.kuku.yuq.pojo.BaiduAIPojo;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.service.ConfigService;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.URLEncoder;

@SuppressWarnings({"SameParameterValue", "unused", "ConstantConditions"})
@Named("baiduAILogic")
public class BaiduAILogicImpl implements AILogic {

	@Inject
	private ConfigService configService;

	private AipOcr getAipOcr(BaiduAIPojo baiduAIPojo){
		return new AipOcr(baiduAIPojo.getOcrAppId(), baiduAIPojo.getOcrAppKey(), baiduAIPojo.getOcrSecretKey());
	}

	private AipContentCensor getAipContentCensor(BaiduAIPojo baiduAIPojo){
		return new AipContentCensor(baiduAIPojo.getContentCensorAppId(),
				baiduAIPojo.getContentCensorAppKey(), baiduAIPojo.getContentCensorSecretKey());
	}

	private AipSpeech getAipSpeech(BaiduAIPojo baiduAIPojo){
		return new AipSpeech(baiduAIPojo.getSpeechAppId(), baiduAIPojo.getSpeechAppKey(),
				baiduAIPojo.getSpeechSecretKey());
	}

	@Override
	public boolean pornIdentification(String imageUrl) throws IOException {
		return pornIdentification(new BaiduAIPojo(), imageUrl);
	}

	private boolean pornIdentification(BaiduAIPojo baiduAIPojo, String imageUrl) throws IOException{
		JSONObject jsonObject = getAipContentCensor(baiduAIPojo).imageCensorUserDefined(OkHttpUtils.getBytes(imageUrl), null);
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
		return generalOCR(new BaiduAIPojo(), imageUrl);
	}

	private String generalOCR(BaiduAIPojo baiduAIPojo, String imageUrl) throws IOException {
		JSONObject jsonObject = getAipOcr(baiduAIPojo).basicGeneral(OkHttpUtils.getBytes(imageUrl), null);
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
	public String textChat(String question, String session) throws IOException {
		return null;
	}

	@Override
	public Result<byte[]> voiceSynthesis(String text) throws IOException {
		return voiceSynthesis(new BaiduAIPojo(), text);
	}

	private Result<byte[]> voiceSynthesis(BaiduAIPojo baiduAIPojo, String text){
		TtsResponse result = getAipSpeech(baiduAIPojo).synthesis(text, "zh", 1, null);
		JSONObject jsonObject = result.getResult();
		if (jsonObject != null){
			return Result.failure(jsonObject.getString("error_msg"), null);
		}else return Result.success(result.getData());
	}

	@Override
	public String voiceIdentify(String voiceUrl) throws IOException {
		return voiceIdentify(new BaiduAIPojo(), voiceUrl);
	}

	private String voiceIdentify(BaiduAIPojo baiduAIPojo, String voiceUrl) throws IOException {
		JSONObject jsonObject = getAipSpeech(baiduAIPojo).asr(OkHttpUtils.getBytes(voiceUrl), "wav", 8000, null);
		if (jsonObject.getInt("err_no") != 0){
			return jsonObject.getString("err_msg");
		}else {
			return jsonObject.getJSONArray("result").getString(0);
		}
	}
}
