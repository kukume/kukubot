@file:Suppress("DuplicatedCode")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Before
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.annotation.QMsg
import com.icecreamqaq.yuq.controller.ContextSession
import com.icecreamqaq.yuq.entity.Contact
import com.icecreamqaq.yuq.entity.Group
import com.icecreamqaq.yuq.message.Message
import com.icecreamqaq.yuq.message.Message.Companion.firstString
import com.icecreamqaq.yuq.message.Message.Companion.toMessage
import com.icecreamqaq.yuq.mif
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kuku.utils.MyUtils
import me.kuku.utils.OkHttpUtils
import me.kuku.utils.QqQrCodeLoginUtils
import me.kuku.yuq.entity.KuGouEntity
import me.kuku.yuq.entity.KuGouService
import me.kuku.yuq.entity.QqEntity
import me.kuku.yuq.logic.KuGouLogic
import me.kuku.yuq.logic.ToolLogic
import java.net.URLDecoder
import javax.inject.Inject

@GroupController
@PrivateController
class KuGouController @Inject constructor(
    private val kuGouService: KuGouService,
    private val kuGouLogic: KuGouLogic,
    private val toolLogic: ToolLogic
) {
    @Before(except = ["qrcode", "loginByCode", "loginByPassword", "qrcodeQq"])
    fun before(qqEntity: QqEntity, qq: Long) = kuGouService.findByQqEntity(qqEntity)
        ?: throw mif.at(qq).plus("您没有绑定酷狗账号，请发送<酷狗二维码>进行绑定").toThrowable()

    @Action("酷狗 {username} {password}")
    fun loginByPassword(qqEntity: QqEntity, username: String, password: String): String{
        var kuGouEntity = kuGouService.findByQqEntity(qqEntity)
        val mid = kuGouEntity?.mid ?: kuGouLogic.mid()
        val result = kuGouLogic.login(username, password, mid)
        return if (result.isSuccess){
            if (kuGouEntity == null) kuGouEntity = KuGouEntity(qqEntity = qqEntity)
            val newKuGouEntity = result.data
            kuGouEntity.token = newKuGouEntity.token
            kuGouEntity.userid = newKuGouEntity.userid
            kuGouEntity.mid = mid
            kuGouEntity.kuGoo = newKuGouEntity.kuGoo
            kuGouService.save(kuGouEntity)
            "绑定酷狗音乐成功！"
        }else "绑定酷狗音乐失败：${result.message}"
    }

    @Action("酷狗验证码 {phone}")
    fun loginByCode(phone: String, session: ContextSession, qq: Contact, qqEntity: QqEntity): String{
        val mid = kuGouLogic.mid()
        val re = kuGouLogic.sendMobileCode(phone, mid)
        return if (re.isFailure) "酷狗发送短信验证码失败：" + re.message
        else {
            qq.sendMessage("请输入验证码！".toMessage())
            val nextMessage = session.waitNextMessage(1000L * 60 * 2)
            val code = nextMessage.firstString()
            val result = kuGouLogic.verifyCode(phone, code, mid)
            if (result.isSuccess){
                val newKuGouEntity = result.data
                val kuGouEntity = kuGouService.findByQqEntity(qqEntity) ?: KuGouEntity(qqEntity = qqEntity)
                kuGouEntity.token = newKuGouEntity.token
                kuGouEntity.userid = newKuGouEntity.userid
                kuGouEntity.mid = mid
                kuGouEntity.kuGoo = newKuGouEntity.kuGoo
                kuGouService.save(kuGouEntity)
                "绑定酷狗音乐成功"
            }else result.message
        }
    }

    @DelicateCoroutinesApi
    @Action("酷狗qq二维码")
    fun qrcodeQq(qqEntity: QqEntity, qq: Long, group: Group){
        var kuGouEntity = kuGouService.findByQqEntity(qqEntity)
        val mid = kuGouEntity?.mid ?: kuGouLogic.mid()
        val response =
            OkHttpUtils.get("https://openplat-user.kugou.com/qq/?appid=1058&dfid=-&platid=4&mid=$mid&force_login=0&callbackurl=https://activity.kugou.com/vipOrgVerify/v-a9193800/mobile.html")
        response.close()
        val redUrl = response.header("location")
        val redirectUrlEn = MyUtils.regex("redirect_uri=", "&", redUrl)
        val redirectUrl = URLDecoder.decode(redirectUrlEn, "utf-8")
        val appid = 716027609L
        val daId = 383
        val aid = 205141L
        val qrCode = QqQrCodeLoginUtils.getQrCode(appid, daId, aid)
        group.sendMessage(mif.at(qq).plus(mif.imageByByteArray(qrCode.bytes)).plus("请使用手机QQ扫码登录！"))
        GlobalScope.launch {
            val msg: Message
            val at = mif.at(qq)
            while (true) {
                delay(3000)
                val result = QqQrCodeLoginUtils.checkQrCode(
                    appid,
                    daId,
                    aid,
                    "https://graph.qq.com/oauth2.0/login_jump",
                    qrCode.sig
                )
                if (result.code == 500) {
                    msg = at.plus(result.message)
                    break;
                }
                if (result.code == 200) {
                    val qqLoginPojo = result.data
                    val res = QqQrCodeLoginUtils.authorize(qqLoginPojo, aid, "state", redirectUrl)
                    if (res.isFailure){
                        msg = at.plus(res.message)
                        break
                    }
                    val url = res.data
                    val resp = OkHttpUtils.get(url)
                    resp.close()
                    val cookie = OkHttpUtils.getCookie(resp)
                    if (kuGouEntity == null) kuGouEntity = KuGouEntity(qqEntity = qqEntity)
                    kuGouEntity!!.token = OkHttpUtils.getCookie(cookie, "t")
                    kuGouEntity!!.mid = mid
                    kuGouEntity!!.kuGoo = OkHttpUtils.getCookie(cookie, "KuGoo")
                    kuGouEntity!!.userid = OkHttpUtils.getCookie(cookie, "KugooID")?.toLong() ?: 0
                    kuGouService.save(kuGouEntity!!)
                    msg = at.plus("绑定酷狗音乐成功！")
                    break
                }
            }
            group.sendMessage(msg)
        }
    }


    @DelicateCoroutinesApi
    @Action("酷狗二维码")
    @QMsg(at = true)
    fun qrcode(group: Group, qqEntity: QqEntity, qq: Long){
        var kuGouEntity = kuGouService.findByQqEntity(qqEntity)
        val mid = kuGouEntity?.mid ?: kuGouLogic.mid()
        val qrcode = kuGouLogic.getQrcode(mid)
        group.sendMessage(mif.at(qq).plus(mif.imageByInputStream(toolLogic.creatQr(qrcode.url)).plus("请使用酷狗音乐APP扫码登录")))
        GlobalScope.launch {
            val msg: Message
            while (true){
                delay(3000)
                val result = kuGouLogic.checkQrcode(qrcode)
                if (result.code == 200){
                    if (kuGouEntity == null) kuGouEntity = KuGouEntity(qqEntity = qqEntity)
                    val newKuGouEntity = result.data
                    kuGouEntity!!.token = newKuGouEntity.token
                    kuGouEntity!!.userid = newKuGouEntity.userid
                    kuGouEntity!!.mid = newKuGouEntity.mid
                    kuGouEntity!!.kuGoo = newKuGouEntity.kuGoo
                    kuGouService.save(kuGouEntity!!)
                    msg = mif.at(qq).plus("绑定酷狗音乐成功")
                    break
                }else if (result.code == 500) {
                    msg = mif.at(qq).plus(result.message)
                    break
                }
            }
            group.sendMessage(msg)
        }
    }

    @Action("酷狗音乐人签到")
    @QMsg(at = true)
    fun musicianSign(kuGouEntity: KuGouEntity): String {
        return kuGouLogic.musicianSign(kuGouEntity).message
    }
}