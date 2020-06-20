package me.kuku.yuq.utils

import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.entity.QQEntity
import me.kuku.yuq.pojo.CommonResult
import org.jsoup.internal.StringUtil
import java.util.*

object QQUtils {

    fun getGtk(sKey: String): Long{
        val len: Int = sKey.length
        var hash = 5381L
        for (i in 0 until len) {
            hash += (hash shl 5 and 2147483647) + sKey[i].toInt() and 2147483647
            hash = hash and 2147483647
        }
        return hash and 2147483647
    }

    fun getGtk2(sKey: String): String{
        var salt: Long = 5381
        val md5key = "tencentQQVIP123443safde&!%^%1282"
        val hash: MutableList<Long?> = ArrayList()
        hash.add(salt shl 5)
        val len = sKey.length
        for (i in 0 until len) {
            val ASCIICode = Integer.toHexString(sKey[i].toInt())
            val code = Integer.valueOf(ASCIICode, 16).toLong()
            hash.add((salt shl 5) + code)
            salt = code
        }
        var md5str = StringUtil.join(hash, "") + md5key
        md5str = MD5Utils.toMD5(md5str)
        return md5str
    }

    fun getToken(token: String): Long{
        val len: Int = token.length
        var hash = 0L
        for (i in 0 until len) {
            hash = (hash * 33 + token[i].toInt()) % 4294967296L
        }
        return hash
    }

    fun convertQQEntity(map: Map<String, String>, qqEntity: QQEntity = QQEntity()): QQEntity{
        qqEntity.sKey = map.getValue("skey")
        qqEntity.psKey = map.getValue("p_skey")
        qqEntity.superKey = map.getValue("superkey")
        qqEntity.superToken = map.getValue("supertoken")
        qqEntity.pt4Token = map.getValue("pt4_token")
        qqEntity.status = true
        return qqEntity
    }

    fun getResultUrl(str: String): CommonResult<String>{
        val msg =  when (BotUtils.regex("'", "','", str)?.toInt()){
            4 -> "验证码错误，登录失败！"
            3 -> "密码错误，登录失败！"
            19 -> "您的QQ号已被冻结，登录失败！"
            10009 -> "您的QQ号登录需要验证短信，请验证短信"
            0,2 ->{
                val url = BotUtils.regex(",'0','", "','", str) ?: BotUtils.regex("','", "'", str)
                if (url != null) return CommonResult(200, "成功", url) else ""
            }
            1 -> "superKey已失效，请更新QQ！"
            else -> BotUtils.regex(",'0','", "', ' '", str)
        }
        return CommonResult(500, msg)
    }

    fun getPtToken(str: String): CommonResult<String>{
        val commonResult = this.getResultUrl(str)
        return if (commonResult.code == 200){
            val url = commonResult.t
            val token = BotUtils.regex("ptsigx=", "&", url)
            CommonResult(200, "成功", token!!)
        }else commonResult
    }

    fun getKey(url: String): Map<String, String>{
        val response = OkHttpClientUtils.get(url)
        response.close()
        return OkHttpClientUtils.getCookie(response, "p_skey", "pt4_token")
    }

    fun getKey(pt: String, qq: String, domain: String, suffixUrl: String): Map<String, String> {
        return this.getKey("https://$domain/check_sig?uin=$qq&ptsigx=$pt$suffixUrl")
    }

    fun saveOrUpdate(qqDao: QQDao, map: Map<String, String>, qq: Long, password: String = "", group: Long = 0L){
        var qqEntity = qqDao.findByQQ(qq) ?: QQEntity()
        qqEntity = this.convertQQEntity(map, qqEntity)
        qqEntity.qq = qq
        if (group != 0L) qqEntity.qqGroup = group
        if (password != "") qqEntity.password = password
        qqDao.singleSave(qqEntity)
    }

}