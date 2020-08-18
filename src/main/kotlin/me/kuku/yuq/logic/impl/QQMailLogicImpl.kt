package me.kuku.yuq.logic.impl

import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.logic.QQMailLogic
import me.kuku.yuq.pojo.CommonResult
import me.kuku.yuq.utils.BotUtils
import me.kuku.yuq.utils.OkHttpClientUtils
import me.kuku.yuq.utils.QQPasswordLoginUtils
import okhttp3.FormBody

class QQMailLogicImpl: QQMailLogic {

    fun login(qqEntity: QQEntity): CommonResult<Map<String, String>> {
        val commonResult = QQPasswordLoginUtils.login("522005705", "4", qqEntity.qq.toString(), qqEntity.password, "https://mail.qq.com/cgi-bin/readtemplate?check=false&t=loginpage_new_jump&vt=passport&vm=wpt&ft=loginpage&target=")
        return if (commonResult.code == 200){
            val psKey = commonResult.t!!.getValue("p_skey")
            val response = OkHttpClientUtils.get("http://mail.qq.com/cgi-bin/login?vt=passport&vm=wpt&ft=loginpage&target=",
                    OkHttpClientUtils.addCookie(qqEntity.getCookie(psKey)))
            val cookie = OkHttpClientUtils.getCookie(response)
            val html = OkHttpClientUtils.getStr(response)
            val sid = BotUtils.regex("sid\\=", "\"", html)
            CommonResult(200, "成功", mapOf("cookie" to cookie, "sid" to sid!!))
        }else commonResult
    }

    override fun getFile(qqEntity: QQEntity): CommonResult<List<Map<String, String>>> {
        val commonResult = this.login(qqEntity)
        return if (commonResult.code == 200){
            val map = commonResult.t!!
            val response = OkHttpClientUtils.get("https://mail.qq.com/cgi-bin/ftnExs_files?sid=${map.getValue("sid")}&t=ftn.json&s=list&ef=js&listtype=self&up=down&sorttype=createtime&page=0&pagesize=50&pagemode=more&pagecount=2&ftnpreload=true&sid=${map.getValue("sid")}",
                    OkHttpClientUtils.addCookie(map.getValue("cookie")))
            val result = OkHttpClientUtils.getStr(response)
            val list = ArrayList<Map<String, String>>().toMutableList()
            val sId = Regex("(?<=sFileId : \").+?(?=\")").findAll(result).iterator()
            val sName = Regex("(?<=sName : \").+?(?=\")").findAll(result).iterator()
            val sKey = Regex("(?<=sKey : \").+?(?=\")").findAll(result).iterator()
            val sCode = Regex("(?<=sFetchCode : \").+?(?=\")").findAll(result).iterator()
            while (sId.hasNext()){
                val fileMap = mutableMapOf("fileId" to sId.next().value, "sName" to sName.next().value, "sKey" to sKey.next().value, "sCode" to sCode.next().value)
                fileMap.putAll(map)
                list.add(fileMap)
            }
            CommonResult(200, "成功", list)
        }else CommonResult(500, commonResult.msg)
    }

    override fun fileRenew(qqEntity: QQEntity): String {
        val commonResult = this.getFile(qqEntity)
        return if (commonResult.code == 200){
            val builder = FormBody.Builder()
            val list = commonResult.t!!
            if (list.isNotEmpty()) {
                val sid = list[0].getValue("sid")
                val cookie = list[0].getValue("cookie")
                for (i in list.indices) {
                    builder.add("fid", list[i].getValue("fileId"))
                }
                OkHttpClientUtils.post("https://mail.qq.com/cgi-bin/ftnExtendfile?sid=$sid&t=ftn.json&s=oper&ef=js&keytext=&sid=$sid",
                        builder.build(), OkHttpClientUtils.addCookie(cookie))
                "QQ邮箱中转站续期成功！共续期了${list.size}个文件"
            }else "您的QQ邮箱中转站还没有文件呢！"
        }else commonResult.msg
    }
}