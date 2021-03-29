package me.kuku.yuq.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import me.kuku.yuq.pojo.UA;
import okhttp3.*;
import okio.ByteString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OkHttpUtils {
    private static final MediaType MEDIA_JSON = MediaType.get("application/json;charset=utf-8");
    private static final MediaType MEDIA_STREAM = MediaType.get("application/octet-stream");
    private static final MediaType MEDIA_X_JSON = MediaType.get("text/x-json");
    private static final long TIME_OUT = 10L;

    private static final OkHttpClient okHttpClient;

    static {
        okHttpClient = new OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .build();
    }

    private static Headers emptyHeaders(){
        return new Headers.Builder().add("User-Agent", UA.PC.getValue()).build();
    }

    public static Response get(String url, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response get(String url, Map<String, String> map) throws IOException {
        return get(url, addHeaders(map));
    }

    public static Response get(String url) throws IOException {
        return get(url, emptyHeaders());
    }

    public static Response post(String url, RequestBody requestBody, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).post(requestBody).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response post(String url, RequestBody requestBody) throws IOException {
        return post(url, requestBody, emptyHeaders());
    }

    public static Response post(String url, Map<String, String> map, Headers headers) throws IOException {
        return post(url, mapToFormBody(map), headers);
    }

    public static Response post(String url, Map<String, String> map) throws IOException {
        return post(url, map, emptyHeaders());
    }

    public static Response post(String url) throws IOException {
        return post(url, new HashMap<>(), emptyHeaders());
    }

    public static Response put(String url, RequestBody requestBody, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).put(requestBody).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    public static Response put(String url, RequestBody requestBody) throws IOException {
        return put(url, requestBody, emptyHeaders());
    }

    private static Response delete(String url, RequestBody requestBody, Headers headers) throws IOException {
        Request request = new Request.Builder().url(url).delete(requestBody).headers(headers).build();
        return okHttpClient.newCall(request).execute();
    }

    private static Response delete(String url, RequestBody requestBody) throws IOException {
        return delete(url, requestBody, emptyHeaders());
    }

    public static Response delete(String url, Map<String, String> map, Headers headers) throws IOException {
        return delete(url, mapToFormBody(map), headers);
    }

    public static Response delete(String url, Map<String, String> map) throws IOException {
        return delete(url, map, emptyHeaders());
    }

    public static String getStr(Response response) throws IOException {
        return Objects.requireNonNull(response.body()).string();
    }

    public static JSONObject getJson(Response response) throws IOException {
        String str = getStr(response);
        return JSON.parseObject(str);
    }

    public static byte[] getBytes(String url) throws IOException {
        return getBytes(url, emptyHeaders());
    }

    public static byte[] getBytes(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getBytes(response);
    }

    public static byte[] getBytes(Response response) throws IOException {
        return Objects.requireNonNull(response.body()).bytes();
    }

    public static InputStream getByteStream(Response response){
        return Objects.requireNonNull(response.body()).byteStream();
    }

    public static InputStream getByteStream(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getByteStream(response);
    }

    public static InputStream getByteStream(String url) throws IOException {
        return getByteStream(url, emptyHeaders());
    }

    private static ByteString getByteStr(Response response) throws IOException {
        return Objects.requireNonNull(response.body()).byteString();
    }

    public static ByteString getByteStr(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getByteStr(response);
    }

    public static ByteString getByteStr(String url) throws IOException {
        return getByteStr(url, emptyHeaders());
    }

    private static InputStream getIs(Response response){
        return Objects.requireNonNull(response.body()).byteStream();
    }

    public static String getStr(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getStr(response);
    }

    public static String getStr(String url) throws IOException {
        Response response = get(url, emptyHeaders());
        return getStr(response);
    }

    public static JSONObject getJson(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getJson(response);
    }

    public static JSONObject getJson(String url, Map<String, String> map) throws IOException {
        return getJson(url, addHeaders(map));
    }

    public static JSONObject getJson(String url) throws IOException {
        Response response = get(url, emptyHeaders());
        return getJson(response);
    }

    public static String postStr(String url, RequestBody requestBody, Headers headers) throws IOException {
        Response response = post(url, requestBody, headers);
        return getStr(response);
    }

    public static String postStr(String url, RequestBody requestBody) throws IOException {
        Response response = post(url, requestBody, emptyHeaders());
        return getStr(response);
    }

    public static JSONObject postJson(String url, RequestBody requestBody, Headers headers) throws IOException {
        Response response = post(url, requestBody, headers);
        return getJson(response);
    }

    public static JSONObject postJson(String url, RequestBody requestBody) throws IOException {
        Response response = post(url, requestBody, emptyHeaders());
        return getJson(response);
    }

    private static RequestBody mapToFormBody(Map<String, String> map){
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry: map.entrySet()){
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static String postStr(String url, Map<String, String> map, Headers headers) throws IOException {
        Response response = post(url, mapToFormBody(map), headers);
        return getStr(response);
    }

    public static String postStr(String url, Map<String, String> map) throws IOException {
        return postStr(url, map, emptyHeaders());
    }

    public static String deleteStr(String url, Map<String, String> map, Headers headers) throws IOException {
        Response response = delete(url, mapToFormBody(map), headers);
        return getStr(response);
    }

    public static String deleteStr(String url, Map<String, String> map) throws IOException {
        return deleteStr(url, map, emptyHeaders());
    }

    public static JSONObject postJson(String url, Map<String, String> map, Headers headers) throws IOException {
        String str = postStr(url, map, headers);
        return JSON.parseObject(str);
    }

    public static JSONObject postJson(String url, Map<String, String> map) throws IOException {
        String str = postStr(url, map, emptyHeaders());
        return JSON.parseObject(str);
    }

    public static JSONObject deleteJson(String url, Map<String, String> map, Headers headers) throws IOException {
        String str = deleteStr(url, map, headers);
        return JSON.parseObject(str);
    }

    public static JSONObject deleteJson(String url, Map<String, String> map) throws IOException {
        String str = deleteStr(url, map, emptyHeaders());
        return JSON.parseObject(str);
    }

    public static JSONObject getJsonp(Response response) throws IOException {
        String str = getStr(response);
        Matcher matcher = Pattern.compile("\\{[\\s\\S]*}").matcher(str);
        if (matcher.find()){
            return JSON.parseObject(matcher.group());
        }else return null;
    }

    public static JSONObject getJsonp(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getJsonp(response);
    }

    public static JSONObject getJsonp(String url) throws IOException {
        return getJsonp(url, emptyHeaders());
    }

    public static RequestBody addJson(String jsonStr){
        return RequestBody.create(jsonStr, MEDIA_JSON);
    }

    public static Headers addSingleHeader(String name, String value){
        return new Headers.Builder().add(name, value).build();
    }

    public static Headers addHeaders(String cookie, String referer, String userAgent){
        if (cookie == null) cookie = "";
        if (referer == null) referer = "";
        if (userAgent == null) userAgent = UA.PC.getValue();
        return new Headers.Builder().add("cookie", cookie).add("referer", referer).add("user-agent", userAgent).build();
    }

    public static Headers addHeaders(String cookie, String referer, UA ua){
        return addHeaders(cookie, referer, ua.getValue());
    }

    public static Headers addHeaders(String cookie, String referer){
        return addHeaders(cookie, referer, UA.PC.getValue());
    }

    public static Headers addHeaders(Map<String, String> map){
        Headers.Builder builder = new Headers.Builder();
        for (Map.Entry<String, String> entry: map.entrySet()){
            builder.add(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    public static Headers.Builder addHeader(){
        return new Headers.Builder();
    }

    public static Headers addUA(UA ua){
        return addSingleHeader("user-agent", ua.getValue());
    }

    public static Headers addUA(String ua){
        return addSingleHeader("user-agent", ua);
    }

    public static Headers addCookie(String cookie){
        return addSingleHeader("cookie", cookie);
    }

    public static Headers addReferer(String url){
        return addSingleHeader("referer", url);
    }

    public static RequestBody addStream(ByteString byteString){
        return RequestBody.create(byteString, MEDIA_STREAM);
    }

    public static RequestBody addStream(String url) throws IOException {
       return addStream(getByteStr(url));
    }

    public static String getCookie(Response response){
        StringBuilder sb = new StringBuilder();
        List<String> cookies = response.headers("Set-Cookie");
        for (String cookie: cookies){
            if (cookie.contains("deleted")) continue;
            cookie = BotUtils.regex(".*?;", cookie);
            sb.append(cookie).append(" ");
        }
        return sb.toString();
    }

    public static String getCookie(String cookie, String name){
        return BotUtils.regex(name + "=", "; ", cookie);
    }

    public static Map<String, String> getCookie(String cookie, String...name){
        Map<String, String> map = new HashMap<>();
        for (String str: name){
            map.put(str, getCookie(cookie, str));
        }
        return map;
    }

    public static String getCookie(String url, Headers headers) throws IOException {
        Response response = get(url, headers);
        return getCookie(response);
    }

    public static String getCookie(String url) throws IOException {
        return getCookie(url, emptyHeaders());
    }

    private static Response download(String url) throws IOException {
        Response response;
        while (true){
            response = get(url);
            int code = response.code();
            if (code == 302 || code == 301){
                response.close();
                url = response.header("location");
            }else break;
        }
        return response;
    }

    public static String downloadStr(String url) throws IOException {
        Response response = download(url);
        return getStr(response);
    }

    public static byte[] downloadBytes(String url) throws IOException {
        Response response = download(url);
        return getBytes(response);
    }

    private static String fileNameByUrl(String url){
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }

    public static RequestBody getStreamBody(String url) throws IOException {
        InputStream is = getByteStream(url);
        return getStreamBody(fileNameByUrl(url), is);
    }

    public static RequestBody getStreamBody(String fileName, InputStream is, boolean isClose){
        try {
            File file = IOUtils.writeTmpFile(fileName, is, isClose);
            return RequestBody.create(file, MEDIA_STREAM);
        } finally {
            if (isClose) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static RequestBody getStreamBody(String fileName, InputStream is){
        return getStreamBody(fileName, is, true);
    }

    public static RequestBody getStreamBody(InputStream is, boolean isClose){
        return getStreamBody(UUID.randomUUID().toString(), is, isClose);
    }

    public static RequestBody getStreamBody(InputStream is){
        return getStreamBody(is, true);
    }

    public static RequestBody getStreamBody(String fileName, byte[] bytes){
        return RequestBody.create(bytes, MEDIA_STREAM);
    }

    public static RequestBody getStreamBody(byte[] bytes){
        return getStreamBody(UUID.randomUUID().toString(), bytes);
    }
}
