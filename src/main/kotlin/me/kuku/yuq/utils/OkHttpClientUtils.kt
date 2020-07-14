package me.kuku.yuq.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object OkHttpClientUtils {
    private val MEDIA_JSON = "application/json;charset=utf-8".toMediaType()
    private val MEDIA_STREAM = "application/octet-stream".toMediaType()
    private const val TIME_OUT = 10L
    const val PC_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36"
    const val MOBILE_UA = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.92 Mobile Safari/537.36"
    const val QQ_UA = "Mozilla/5.0 (Linux; Android 10; V1914A Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/6.2 TBS/045132 Mobile Safari/537.36 V1_AND_SQ_8.3.0_1362_YYB_D QQ/8.3.0.4480 NetType/4G WebP/0.3.0 Pixel/1080 StatusBarHeight/85 SimpleUISwitch/0 QQTheme/1000"
    const val QQ_UA2 = "Mozilla/5.0 (Linux; Android 10; V1914A Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045224 Mobile Safari/537.36 V1_AND_SQ_8.3.9_1424_YYB_D QQ/8.3.9.4635 NetType/4G WebP/0.3.0 Pixel/1080 StatusBarHeight/85 SimpleUISwitch/0 QQTheme/1000"
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
            .readTimeout(TIME_OUT, TimeUnit.SECONDS)
            .build()

    fun get(url: String, headers: Headers = Headers.Builder().build()): Response{
        val request = Request.Builder().url(url).headers(headers).build()
        return okHttpClient.newCall(request).execute()
    }

    fun post(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = Headers.Builder().build()): Response{
        val request = Request.Builder().url(url).post(requestBody).headers(headers).build()
        return okHttpClient.newCall(request).execute()
    }

    fun put(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = Headers.Builder().build()): Response{
        val request = Request.Builder().url(url).put(requestBody).headers(headers).build()
        return okHttpClient.newCall(request).execute()
    }

    fun delete(url: String, requestBody: RequestBody = FormBody.Builder().build(), headers: Headers = Headers.Builder().build()): Response{
        val request = Request.Builder().url(url).delete(requestBody).headers(headers).build()
        return okHttpClient.newCall(request).execute()
    }

    fun addHeader(name: String, value: String) = Headers.Builder().add(name, value).build()

    fun addCookie(value: String) = this.addHeader("cookie", value)

    fun addReferer(value: String) = this.addHeader("Referer", value)

    fun addUA(value: String) = this.addHeader("user-agent", value)

    fun getCookie(response: Response, vararg names: String): Map<String, String>{
        val map = HashMap<String, String>()
        val cookies = response.headers("Set-Cookie")
        for (cookie in cookies){
            val newCookie = BotUtils.regex(".*?;", cookie)?.removeSuffix(";")
            val arr = newCookie!!.split("=")
            for (name in names){
                if (name == arr[0] && arr[1] != "") {
                    map[arr[0]] = arr[1]
                }
            }
        }
        return map
    }

    fun getCookie(response: Response): String{
        val sb = StringBuilder()
        val cookies = response.headers("Set-Cookie")
        for (cookie in cookies){
            sb.append("${BotUtils.regex(".*?;", cookie)} ")
        }
        return sb.toString()
    }

    fun addHeaders(vararg headers: String): Headers{
        val builder = Headers.Builder()
        for (i in headers.indices step 2){
            builder.add(headers[i], headers[i+1])
        }
        return builder.build()
    }

    fun addForms(vararg forms: String): RequestBody{
        val builder = FormBody.Builder()
        for (i in forms.indices step 2){
            builder.add(forms[i], forms[i+1])
        }
        return builder.build()
    }

    fun addStream(url: String): RequestBody{
        return this.getByteStr(this.get(url)).toRequestBody(this.MEDIA_STREAM)
    }

    fun addJson(params: String) = params.toRequestBody(MEDIA_JSON)

    fun getStr(response: Response) = response.body!!.string()

    fun getStr(response: Response, regex: String) = BotUtils.regex(regex, this.getStr(response))

    fun getJson(response: Response): JSONObject = JSON.parseObject(this.getStr(response))

    fun getJson(response: Response, regex: String): JSONObject = JSON.parseObject(this.getStr(response, regex))

    fun getJsonp(response: Response) = this.getJson(response, "\\{[\\s\\S]*\\}")

    fun getBytes(response: Response) = response.body!!.bytes()

    fun getByteStr(response: Response) = response.body!!.byteString()

}