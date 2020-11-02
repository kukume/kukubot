package me.kuku.yuq.utils

import com.IceCreamQAQ.Yu.util.OkHttpWebImpl
import okhttp3.FormBody
import okhttp3.Request

fun StringBuilder.removeSuffixLine() = this.removeSuffix("\n")

fun OkHttpWebImpl.postQQUA(url: String, para: Map<String, String>): String{
    val fbBuilder = FormBody.Builder()
    for (s in para.keys) {
        fbBuilder.add(s, para[s] ?: "")
    }
    val formBody = fbBuilder.build()
    val request = Request.Builder().post(formBody).addHeader("user-agent", "Mozilla/5.0 (Linux; Android 10; V1914A Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.3359.126 MQQBrowser/6.2 TBS/045132 Mobile Safari/537.36 V1_AND_SQ_8.3.0_1362_YYB_D QQ/8.3.0.4480 NetType/4G WebP/0.3.0 Pixel/1080 StatusBarHeight/85 SimpleUISwitch/0 QQTheme/1000").url(url).build()
    val call = client.newCall(request)
    val response = call.execute()
    return response.body!!.string()
}
