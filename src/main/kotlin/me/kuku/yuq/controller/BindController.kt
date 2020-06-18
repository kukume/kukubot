package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import me.kuku.yuq.dao.QQDao
import me.kuku.yuq.utils.QQPasswordLoginUtils
import me.kuku.yuq.utils.QQUtils
import javax.inject.Inject

@PrivateController
class BindController {
    @Inject
    private lateinit var qqDao: QQDao

    @Action("qq")
    fun bindQQ(@PathVar(1) password: String?, qq: Long): Any? {
        return if (password != null){
            val commonResult = QQPasswordLoginUtils.login(qq = qq.toString(), password = password)
            if (commonResult.code == 200){
                val map = commonResult.t
                QQUtils.saveOrUpdate(qqDao, map, qq, password)
                "绑定或者更新成功！"
            }else commonResult.msg
        }else "请输入QQ密码"
    }

}