package me.kuku.yuq.logic.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.logic.TeambitionLogic;
import me.kuku.yuq.pojo.Result;
import me.kuku.yuq.pojo.TeambitionPojo;
import me.kuku.yuq.pojo.UA;
import me.kuku.yuq.utils.BotUtils;
import me.kuku.yuq.utils.FileUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"SpellCheckingInspection", "DuplicatedCode"})
public class TeambitionLogicImpl implements TeambitionLogic {

	//https://www.teambition.com/project/60112787ada80cf27620e452/works/60112787ada80cf27620e453
	//                                       projectId                   parentId
	@Override
	public Result<TeambitionPojo> login(String phone, String password) throws IOException {
		String url = "https://account.teambition.com/login/password";
		Response response = OkHttpUtils.get(url,
				OkHttpUtils.addUA(UA.PC));
		String html = OkHttpUtils.getStr(response);
		Element element = Jsoup.parse(html).getElementById("accounts-config");
		String jsonStr = BotUtils.regex("\\{.*\\}", element.toString());
		JSONObject htmlJsonObject = JSON.parseObject(jsonStr);
		String token = htmlJsonObject.getString("TOKEN");
		String cookie = OkHttpUtils.getCookie(response);
		JSONObject params = new JSONObject();
		params.put("client_id", "90727510-5e9f-11e6-bf41-15ed35b6cc41");
		params.put("password", password);
		params.put("phone", phone);
		params.put("response_type", "session");
		params.put("token", token);
		Response loginReponse = OkHttpUtils.post("https://account.teambition.com/api/login/phone",
				OkHttpUtils.addJson(JSON.toJSONString(params)),
				OkHttpUtils.addHeaders(cookie, url, UA.PC));
		if (loginReponse.code() == 200){
			cookie += OkHttpUtils.getCookie(loginReponse);
			String hh = OkHttpUtils.getStr("https://www.teambition.com/todo", OkHttpUtils.addHeaders(cookie, "", UA.PC));
			hh = Jsoup.parse(hh).getElementById("teambition-config").text();
			JSONObject loginSucJsonObject = JSON.parseObject(hh);
			String auth = loginSucJsonObject.getJSONObject("userInfo").getString("strikerAuth");
			return Result.success(new TeambitionPojo(cookie, auth));
		}else {
			return Result.failure("");
		}
	}

	private String getFolderId(TeambitionPojo teambitionPojo, String parentId, String projectId, String name) throws IOException {
		String str = OkHttpUtils.getStr("https://www.teambition.com/api/collections?_parentId=" + parentId
				+ "&_projectId=" + projectId + "&order=updatedDesc&count=50&page=1&_=" + System.currentTimeMillis(),
				OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		JSONArray jsonArray = JSON.parseArray(str);
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			if (jsonObject.getString("title").equals(name)){
				return jsonObject.getString("_id");
			}
		}
		return null;
	}

	public Result<Map<String, String>> project(TeambitionPojo teambitionPojo, String name) throws IOException {
		Response response = OkHttpUtils.get("https://www.teambition.com/api/v2/projects?_organizationId=000000000000000000000405&selectBy=joined&orderBy=name&pageToken=&pageSize=20&_=" +
				System.currentTimeMillis(), OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		String str = OkHttpUtils.getStr(response);
		JSONObject jsonObject = JSON.parseObject(str);
		JSONArray jsonArray = jsonObject.getJSONArray("result");
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			if (singleJsonObject.getString("name").equals(name)){
				Map<String, String> map = new HashMap<>();
				map.put("id", singleJsonObject.getString("_id"));
				map.put("rootId", singleJsonObject.getString("_rootCollectionId"));
				return Result.success(map);
			}
		}
		return Result.failure("没有查询到这个项目名称！！");
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public Result<String> uploadToProject(TeambitionPojo teambitionPojo, String projectName, byte[] bytes, String...path) throws IOException {
		Result<Map<String, String>> projectResult = project(teambitionPojo, projectName);
		if (projectResult.isFailure()) return Result.failure("没有查询到这个项目Id");
		if (path.length == 0) return Result.failure("参数不正确！！");
		Map<String, String> projectMap = projectResult.getData();
		String parentId = getFinallyParentId(teambitionPojo, projectMap.get("rootId"), projectMap.get("id"), path);
		String fileType = FileUtils.getFileTypeByStream(bytes);
		String fileName = path[path.length - 1];
		if (!fileName.contains(".")) fileName += "." + fileType;
		MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("name", fileName)
				.addFormDataPart("type", fileType)
				.addFormDataPart("size", String.valueOf(bytes.length))
				.addFormDataPart("lastModifiedDate", "Mon Dec 28 2020 12:56:59 GMT+0800 (中国标准时间)")
				.addFormDataPart("file", fileName, RequestBody.create(bytes)).build();
		Map<String, String> map = new HashMap<>();
		map.put("Authorization", teambitionPojo.getStrikerAuth());
		JSONObject jsonObject = OkHttpUtils.postJson("https://tcs.teambition.net/upload", body,
				OkHttpUtils.addHeaders(map));
		JSONObject params = new JSONObject();
		JSONArray works = new JSONArray();
		JSONObject worksJsonObject = new JSONObject();
		worksJsonObject.put("fileKey", jsonObject.getString("fileKey"));
		worksJsonObject.put("fileName", jsonObject.getString("fileName"));
		worksJsonObject.put("fileType", jsonObject.getString("fileType"));
		worksJsonObject.put("fileSize", jsonObject.getString("fileSize"));
		worksJsonObject.put("fileCategory", jsonObject.getString("fileCategory"));
		worksJsonObject.put("imageWidth", jsonObject.getString("imageWidth"));
		worksJsonObject.put("imageHeight", jsonObject.getString("imageHeight"));
		worksJsonObject.put("source", jsonObject.getString("source"));
		worksJsonObject.put("visible", "members");
		worksJsonObject.put("_parentId", parentId);
		worksJsonObject.put("involveMembers", new JSONArray());
		works.add(worksJsonObject);
		params.put("works", works);
		params.put("_parentId", parentId);
		String uploadStr = OkHttpUtils.postStr("https://www.teambition.com/api/works", OkHttpUtils.addJson(params.toString()),
				OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		JSONArray uploadJsonArray = JSON.parseArray(uploadStr);
		return Result.success(uploadJsonArray.getJSONObject(0).getString("downloadUrl"));
	}

	private String getFinallyParentId(TeambitionPojo teambitionPojo, String rootParentId, String projectId, String...path) throws IOException {
		String finallyParentId = rootParentId;
		if (path.length != 1) {
			for (int i = 0; i < path.length - 1; i++) {
				String name = path[i];
				finallyParentId = getFolderId(teambitionPojo, finallyParentId, projectId, name);
			}
		}
		return finallyParentId;
	}

	@Override
	public Result<String> fileDownloadUrl(TeambitionPojo teambitionPojo, String projectName, String...path) throws IOException {
		Result<Map<String, String>> projectResult = project(teambitionPojo, projectName);
		if (projectResult.isFailure()) return Result.failure("没有查询到这个项目Id");
		if (path.length == 0) return Result.failure("参数不正确！！");
		Map<String, String> map = projectResult.getData();
		String parentId = map.get("rootId");
		String projectId = map.get("id");
		String finallyParentId = getFinallyParentId(teambitionPojo, parentId, projectId, path);
		String str = OkHttpUtils.getStr("https://www.teambition.com/api/works?_parentId=" + finallyParentId +
				"&_projectId=" + projectId + "&order=updatedDesc&count=50&page=1&_=" + System.currentTimeMillis(),
				OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		JSONArray jsonArray = JSON.parseArray(str);
		String fileName = path[path.length - 1];
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			if (jsonObject.getString("fileName").equals(fileName)){
				return Result.success(jsonObject.getString("downloadUrl"));
			}
		}
		return Result.failure("没有找到这个文件！！");
	}
}
