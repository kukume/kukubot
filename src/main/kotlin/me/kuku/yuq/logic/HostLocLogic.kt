package me.kuku.automatic.logic

import kotlinx.coroutines.delay
import me.kuku.pojo.Result
import me.kuku.pojo.UA
import me.kuku.utils.JobManager
import me.kuku.utils.MyUtils
import me.kuku.utils.OkHttpUtils
import me.kuku.utils.OkUtils
import org.jsoup.Jsoup
import java.io.IOException

object HostLocLogic {

    fun login(username: String, password: String): Result<String> {
        val map = mapOf(
            "fastloginfield" to "username", "username" to username, "cookietime" to "2592000",
            "password" to password, "quickforward" to "yes", "handlekey" to "ls"
        )
        val response = OkHttpUtils.post("https://hostloc.com/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1",
            map, OkUtils.headers("", "https://hostloc.com/forum.php", UA.PC))
        val str = OkUtils.str(response)
        return if (str.contains("https://hostloc.com/forum.php"))
            Result.success(OkUtils.cookie(response))
        else Result.failure("账号或密码错误或其他原因登录失败！")
    }

    fun isLogin(cookie: String): Boolean {
        val html = OkHttpUtils.getStr("https://hostloc.com/home.php?mod=spacecp",
            OkUtils.headers(cookie, "", UA.PC))
        val text = Jsoup.parse(html).getElementsByTag("title").first()!!.text()
        return text.contains("个人资料")
    }

    fun sign(cookie: String) {
        JobManager.now{
            val urlList = mutableListOf<String>()
            for (i in 0..12) {
                val num = MyUtils.randomInt(10000, 50000)
                urlList.add("https://hostloc.com/space-uid-$num.html")
            }
            for (url in urlList) {
                delay(5000)
                try {
                    OkHttpUtils.get(url, OkUtils.headers(cookie, "https://hostloc.com/forum.php", UA.PC))
                        .close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun post(): List<HostLocPost> {
        val list = mutableListOf<HostLocPost>()
        val html = kotlin.runCatching {
            OkHttpUtils.getStr("https://hostloc.com/forum.php?mod=forumdisplay&fid=45&filter=author&orderby=dateline",
                OkUtils.headers("", "https://hostloc.com/forum.php", UA.PC))
        }.onFailure {
            return list
        }
        val elements = Jsoup.parse(html.getOrThrow()).getElementsByTag("tbody")
        for (ele in elements) {
            if (!ele.attr("id").startsWith("normalth")) continue
            val s = ele.getElementsByClass("s").first()!!
            val title = s.text()
            val url = "https://hostloc.com/${s.attr("href")}"
            val time = ele.select("em a span").first()?.text() ?: ""
            val name = ele.select("cite a").first()?.text() ?: ""
            val id = MyUtils.regex("tid=", "&", url)?.toInt() ?: 0
            val hostLocPost = HostLocPost(id, name, time, title, url)
            list.add(hostLocPost)
        }
        return list
    }

    fun postContent(url: String): String {
        val str = OkHttpUtils.getStr(url, OkUtils.ua(UA.PC))
        val pct = Jsoup.parse(str).getElementsByClass("pct")
        return pct.first()?.text() ?: "未获取到内容，需要权限查看"
    }

}


data class HostLocPost(
    val id: Int,
    val name: String,
    val time: String,
    val title: String,
    val url: String
)