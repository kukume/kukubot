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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
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
		Response loginResponse = OkHttpUtils.post("https://account.teambition.com/api/login/phone",
				OkHttpUtils.addJson(JSON.toJSONString(params)),
				OkHttpUtils.addHeaders(cookie, url, UA.PC));
		if (loginResponse.code() == 200){
			loginResponse.close();
			cookie += OkHttpUtils.getCookie(loginResponse);
			TeambitionPojo teambitionPojo = new TeambitionPojo();
			teambitionPojo.setCookie(cookie);
			teambitionPojo = getAuth(teambitionPojo).getData();
			return Result.success(teambitionPojo);
		}else {
			JSONObject jsonObject = OkHttpUtils.getJson(loginResponse);
			return Result.failure(jsonObject.getString("message"));
		}
	}

	@Override
	public Result<TeambitionPojo> getAuth(TeambitionPojo teambitionPojo) throws IOException {
		Response response = OkHttpUtils.get("https://www.teambition.com/todo", OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		if (response.code() != 200){
			response.close();
			return Result.failure(501, "cookie已失效，请重新登录！！");
		}
		String hh = OkHttpUtils.getStr(response);
		hh = Jsoup.parse(hh).getElementById("teambition-config").text();
		JSONObject loginSucJsonObject = JSON.parseObject(hh);
		String auth = loginSucJsonObject.getJSONObject("userInfo").getString("strikerAuth");
		teambitionPojo.setStrikerAuth(auth);
		return Result.success(teambitionPojo);
	}

	private Result<String> getFolderId(TeambitionPojo teambitionPojo, String parentId, String name, boolean isCreate) throws IOException {
		String projectId = teambitionPojo.getProjectId();
		String str = OkHttpUtils.getStr("https://www.teambition.com/api/collections?_parentId=" + parentId
						+ "&_projectId=" + projectId + "&order=updatedDesc&count=50&page=1&_=" + System.currentTimeMillis(),
				OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		try {
			JSONArray jsonArray = JSON.parseArray(str);
			for (int i = 0; i < jsonArray.size(); i++){
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.getString("title").equals(name)){
					return Result.success(jsonObject.getString("_id"));
				}
			}
		} catch (Exception e) {
			JSONObject jsonObject = JSON.parseObject(str);
			String message = jsonObject.getString("message");
			if (message.contains("无权限操作资源") || "InvalidCookie".equals(jsonObject.getString("name"))) return Result.failure(501, "cookie已失效，请重新登录！！");
			else return Result.failure(message);
		}
		if (isCreate)
			return Result.success(creatFolder(teambitionPojo, parentId, name));
		else return Result.failure("文件夹不存在！！");
	}

	@Override
	public Result<TeambitionPojo> project(TeambitionPojo teambitionPojo, String name) throws IOException {
		Response response = OkHttpUtils.get("https://www.teambition.com/api/v2/projects?_organizationId=000000000000000000000405&selectBy=joined&orderBy=name&pageToken=&pageSize=20&_=" +
				System.currentTimeMillis(), OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		if (response.code() != 200){
			response.close();
			return Result.failure(501, "cookie已失效，请重新登录！！");
		}
		String str = OkHttpUtils.getStr(response);
		JSONObject jsonObject = JSON.parseObject(str);
		JSONArray jsonArray = jsonObject.getJSONArray("result");
		for (int i = 0; i < jsonArray.size(); i++){
			JSONObject singleJsonObject = jsonArray.getJSONObject(i);
			if (singleJsonObject.getString("name").equals(name)){
				teambitionPojo.setProjectId(singleJsonObject.getString("_id"));
				teambitionPojo.setRootId(singleJsonObject.getString("_rootCollectionId"));
				return Result.success(teambitionPojo);
			}
		}
		return Result.failure("没有查询到这个项目名称！！");
	}

	@Override
	public Result<String> uploadToProject(TeambitionPojo teambitionPojo, InputStream is, String...path) throws IOException {
		if (path.length == 0) return Result.failure("参数不正确！！");
		Result<String> parentIdResult = getFinallyParentId(teambitionPojo, true, path);
		if (parentIdResult.isFailure()) return parentIdResult;
		String parentId = parentIdResult.getData();
		String fileType = FileUtils.getFileTypeByStream(is);
		String fileName = path[path.length - 1];
		if (!fileName.contains(".")) fileName += "." + fileType;
		MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("name", fileName)
				.addFormDataPart("type", fileType)
				.addFormDataPart("size", String.valueOf(is.available()))
				.addFormDataPart("lastModifiedDate", "Mon Dec 28 2020 12:56:59 GMT+0800 (中国标准时间)")
				.addFormDataPart("file", fileName, OkHttpUtils.getStreamBody(is)).build();
		Map<String, String> map = new HashMap<>();
		map.put("Authorization", teambitionPojo.getStrikerAuth());
		JSONObject jsonObject = OkHttpUtils.postJson("https://tcs.teambition.net/upload", body,
				OkHttpUtils.addHeaders(map));
		if (jsonObject.containsKey("error")){
			// token/ auth失效
			return Result.failure(501, jsonObject.getString("message"));
		}
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

	private Result<String> getFinallyParentId(TeambitionPojo teambitionPojo, boolean isCreate, String...path) throws IOException {
		String finallyParentId = teambitionPojo.getRootId();
		if (path.length != 1) {
			for (int i = 0; i < path.length - 1; i++) {
				String name = path[i];
				Result<String> result = getFolderId(teambitionPojo, finallyParentId, name, isCreate);
				if (result.isFailure()) return result;
				finallyParentId = result.getData();
			}
		}
		return Result.success(finallyParentId);
	}

	private String creatFolder(TeambitionPojo teambitionPojo, String parentId, String name) throws IOException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("collectionType", "");
		jsonObject.put("color", "blue");
		jsonObject.put("created", "");
		jsonObject.put("description", "");
		jsonObject.put("objectType", "collection");
		jsonObject.put("recentWorks", new JSONArray());
		jsonObject.put("subCount", null);
		jsonObject.put("title", name);
		jsonObject.put("updated", "");
		jsonObject.put("workCount", 0);
		jsonObject.put("_creatorId", "");
		jsonObject.put("_parentId", parentId);
		jsonObject.put("_projectId", teambitionPojo.getProjectId());
		JSONObject resultJsonObject = OkHttpUtils.postJson("https://www.teambition.com/api/collections",
				OkHttpUtils.addJson(jsonObject.toString()), OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		return resultJsonObject.getString("_id");
	}

	@Override
	public Result<String> fileDownloadUrl(TeambitionPojo teambitionPojo, String...path) throws IOException {
		if (path.length == 0) return Result.failure("参数不正确！！");
		String projectId = teambitionPojo.getProjectId();
		Result<String> finallyParentIdResult = getFinallyParentId(teambitionPojo,false, path);
		if (finallyParentIdResult.isFailure()) return finallyParentIdResult;
		String finallyParentId = finallyParentIdResult.getData();
		int page = 1;
		while (true) {
			String str = OkHttpUtils.getStr("https://www.teambition.com/api/works?_parentId=" + finallyParentId +
							"&_projectId=" + projectId + "&order=updatedDesc&count=50&page=" + page++ + "&_=" + System.currentTimeMillis(),
					OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
			JSONArray jsonArray;
			try {
				jsonArray = JSON.parseArray(str);
			}catch (Exception e){
				JSONObject jsonObject = JSON.parseObject(str);
				String name = jsonObject.getString("name");
				if ("InvalidCookie".equals(name)) return Result.failure(501, "cookie已失效");
				else return Result.failure(jsonObject.getString("message"));
			}
			String fileName = path[path.length - 1];
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.getString("fileName").equals(fileName)) {
					return Result.success(jsonObject.getString("downloadUrl"));
				}
			}
			if (jsonArray.size() < 50) break;
		}
		return Result.failure("没有找到这个文件！！");
	}

	@Override
	public Result<TeambitionPojo> getPanInfo(TeambitionPojo teambitionPojo) throws IOException {
		String cookie = teambitionPojo.getCookie();
		JSONObject jsonObject = OkHttpUtils.getJson("https://www.teambition.com/api/v2/roles?type=organization&_=1614951897223",
				OkHttpUtils.addCookie(cookie));
		if (jsonObject.containsKey("name")){
			String errName = jsonObject.getString("name");
			if ("InvalidCookie".equals(errName)) return Result.failure(501, "cookie已失效，请重新登录！！");
			return Result.failure(jsonObject.getString("message"));
		}
		String orgId = jsonObject.getJSONObject("result").getJSONArray("roles")
				.getJSONObject(0).getString("_organizationId");
		JSONObject userJsonObject = OkHttpUtils.getJson("https://pan.teambition.com/pan/api/orgs/" + orgId + "/members/getByUser?orgId=" +
				orgId, OkHttpUtils.addCookie(cookie));
		String userId = userJsonObject.getString("_userId");
		String str = OkHttpUtils.getStr("https://pan.teambition.com/pan/api/spaces?orgId=" + orgId + "&memberId=" +
				userId, OkHttpUtils.addCookie(cookie));
		JSONArray spaceJsonArray = JSON.parseArray(str);
		if (spaceJsonArray.size() == 0) return Result.failure("您的账号没有使用网盘的资格！！");
		JSONObject spaceJsonObject = spaceJsonArray.getJSONObject(0);
		String spaceId = spaceJsonObject.getString("spaceId");
		String rootId = spaceJsonObject.getString("rootId");
		JSONObject driveJsonObject = OkHttpUtils.getJson("https://pan.teambition.com/pan/api/orgs/" + orgId + "?orgId=" + orgId,
				OkHttpUtils.addCookie(cookie));
		String driveId = driveJsonObject.getJSONObject("data").getString("driveId");
		teambitionPojo.setPanDriveId(driveId);
		teambitionPojo.setPanRootId(rootId);
		teambitionPojo.setPanOrgId(orgId);
		teambitionPojo.setPanSpaceId(spaceId);
		teambitionPojo.setUserId(userId);
		return Result.success(teambitionPojo);
	}

	private Result<JSONObject> panNode(TeambitionPojo teambitionPojo, String parentId, String from) throws IOException {
		if (parentId == null) parentId = teambitionPojo.getPanRootId();
		JSONObject jsonObject = OkHttpUtils.getJson("https://pan.teambition.com/pan/api/nodes?orgId=" +
				teambitionPojo.getPanOrgId() + "&from=" + from +
				"&limit=100&orderBy=updated_at&orderDirection=DESC&driveId=" + teambitionPojo.getPanDriveId() + "&parentId=" +
				parentId, OkHttpUtils.addHeaders(teambitionPojo.getCookie(), "", UA.PC));
		if (jsonObject.containsKey("status")){
			Integer status = jsonObject.getInteger("status");
			if (status == 401) return Result.failure(501, "cookie已失效，请重新登录！！");
			else return Result.failure(jsonObject.getString("message"));
		}
		return Result.success(jsonObject);
	}

	private Result<JSONObject> panGetFileOrFolder(TeambitionPojo teambitionPojo, String parentId, String from, String name, boolean isFile, boolean isCreate) throws IOException {
		Result<JSONObject> result = panNode(teambitionPojo, parentId, from);
		if (result.isFailure()) return result;
		JSONObject jsonObject = result.getData();
		JSONArray dataJsonArray = jsonObject.getJSONArray("data");
		for (int i = 0; i < dataJsonArray.size(); i++){
			JSONObject singleJsonObject = dataJsonArray.getJSONObject(i);
			String kind = singleJsonObject.getString("kind");
			String panName = singleJsonObject.getString("name");
			if (isFile){
				if (kind.equals("file") && panName.equals(name)){
					return Result.success(singleJsonObject);
				}
			}else {
				if (kind.equals("folder")){
					if (panName.equals(name)){
						return Result.success(singleJsonObject);
					}
				}else break;
			}
		}
		String next = jsonObject.getString("nextMarker");
		if ("".equals(next)){
			if (isCreate) return Result.success(panCreatFolder(teambitionPojo, parentId, name));
			else return Result.failure("没有找到这个文件或者文件夹！！");
		}else {
			return panGetFileOrFolder(teambitionPojo, parentId, next, name, isFile, isCreate);
		}
	}

	private Result<String> panGetFinallyParentId(TeambitionPojo teambitionPojo, String parentId, boolean isCreate, String...path) throws IOException {
		for (int i = 0; i < path.length - 1; i++){
			String p = path[i];
			Result<JSONObject> result = panGetFileOrFolder(teambitionPojo, parentId, "", p, false, isCreate);
			if (result.isFailure()) return Result.failure(result.getMessage());
			JSONObject jsonObject = result.getData();
			parentId = jsonObject.getString("nodeId");
		}
		return Result.success(parentId);
	}

	private Result<String> panGetFinallyParentId(TeambitionPojo teambitionPojo, String parentId, String...path) throws IOException {
		return panGetFinallyParentId(teambitionPojo, parentId, false, path);
	}

	@Override
	public Result<String> panFileDownloadUrl(TeambitionPojo teambitionPojo, String... path) throws IOException {
		if (path.length == 0) return Result.failure("参数不正确！！");
		String parentId = teambitionPojo.getPanRootId();
		if (path.length != 1){
			Result<String> result = panGetFinallyParentId(teambitionPojo, parentId, path);
			if (result.isFailure()) return result;
			parentId = result.getData();
		}
		Result<JSONObject> result = panGetFileOrFolder(teambitionPojo, parentId, "", path[path.length - 1], true, false);
		if (result.isFailure()) return Result.failure(result.getMessage());
		JSONObject resultJsonObject = result.getData();
		return Result.success(resultJsonObject.getString("downloadUrl"));
	}

	@SuppressWarnings("UnnecessaryLocalVariable")
	private JSONObject panCreatFolder(TeambitionPojo teambitionPojo, String parentId, String name) throws IOException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ccpParentId", parentId);
		jsonObject.put("checkNameMode", "refuse");
		jsonObject.put("driveId", teambitionPojo.getPanDriveId());
		jsonObject.put("name", name);
		jsonObject.put("orgId", teambitionPojo.getPanOrgId());
		jsonObject.put("parentId", parentId);
		jsonObject.put("spaceId", teambitionPojo.getPanSpaceId());
		jsonObject.put("type", "folder");
		String str = OkHttpUtils.postStr("https://pan.teambition.com/pan/api/nodes/folder",
				OkHttpUtils.addJson(jsonObject.toString()), OkHttpUtils.addCookie(teambitionPojo.getCookie()));
		JSONObject returnJsonObject = JSON.parseArray(str).getJSONObject(0);
		return returnJsonObject;
	}

	@Override
	public Result<Boolean> panUploadFile(TeambitionPojo teambitionPojo, InputStream is, String... path) throws IOException {
		if (path.length == 0) return Result.failure("参数不正确！！");
		Result<String> result = panGetFinallyParentId(teambitionPojo, teambitionPojo.getPanRootId(), true, path);
		if (result.isFailure()) return Result.failure(result.getCode(), result.getMessage());
		String parentId = result.getData();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("orgId", teambitionPojo.getPanOrgId());
		jsonObject.put("spaceId", teambitionPojo.getPanSpaceId());
		jsonObject.put("parentId", parentId);
		jsonObject.put("checkNameMode", "autoRename");
		JSONArray jsonArray = new JSONArray();
		JSONObject innerJsonObject = new JSONObject();
		innerJsonObject.put("driveId", teambitionPojo.getPanDriveId());
		innerJsonObject.put("chunkCount", 1);
		innerJsonObject.put("name", path[path.length - 1]);
		innerJsonObject.put("ccpParentId", parentId);
		innerJsonObject.put("contentType", FileUtils.getFileTypeByStream(is));
		innerJsonObject.put("size", is.available());
		innerJsonObject.put("type", "file");
		jsonArray.add(innerJsonObject);
		jsonObject.put("infos", jsonArray);
		String fileStr = OkHttpUtils.postStr("https://pan.teambition.com/pan/api/nodes/file",
				OkHttpUtils.addJson(jsonObject.toString()), OkHttpUtils.addCookie(teambitionPojo.getCookie()));
		JSONObject fileJsonObject = JSON.parseArray(fileStr).getJSONObject(0);
		String uploadId = fileJsonObject.getString("uploadId");
		JSONObject uploadUrlJsonObject = new JSONObject();
		uploadUrlJsonObject.put("driveId", teambitionPojo.getPanDriveId());
		uploadUrlJsonObject.put("endPartNumber", 1);
		uploadUrlJsonObject.put("orgId", teambitionPojo.getPanOrgId());
		uploadUrlJsonObject.put("startPartNumber", 1);
		uploadUrlJsonObject.put("uploadId", uploadId);
		JSONObject uploadUrlResultJsonObject = OkHttpUtils.postJson("https://pan.teambition.com/pan/api/nodes/" +
						fileJsonObject.getString("nodeId")+ "/uploadUrl",
				OkHttpUtils.addJson(uploadUrlJsonObject.toString()), OkHttpUtils.addCookie(teambitionPojo.getCookie()));
		String uploadUrl = uploadUrlResultJsonObject.getJSONArray("partInfoList").getJSONObject(0).getString("uploadUrl");
		OkHttpUtils.put(uploadUrl, OkHttpUtils.getStreamBody(is)).close();
		JSONObject completeJsonObject = new JSONObject();
		completeJsonObject.put("ccpFileId", fileJsonObject.getString("ccpFileId"));
		completeJsonObject.put("driveId", teambitionPojo.getPanDriveId());
		completeJsonObject.put("nodeId", fileJsonObject.getString("nodeId"));
		completeJsonObject.put("orgId", teambitionPojo.getPanOrgId());
		completeJsonObject.put("uploadId", uploadId);
		JSONObject completeResultJsonObject = OkHttpUtils.postJson("https://pan.teambition.com/pan/api/nodes/complete",
				OkHttpUtils.addJson(completeJsonObject.toString()), OkHttpUtils.addCookie(teambitionPojo.getCookie()));
		if (completeResultJsonObject.getInteger("id") == 0)
			return Result.success(true);
		else return Result.failure(completeResultJsonObject.getString("message"));
	}
}
