package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.icecreamqaq.yuq.annotation.GroupController
import com.icecreamqaq.yuq.annotation.PathVar
import com.icecreamqaq.yuq.annotation.PrivateController
import com.icecreamqaq.yuq.mif
import org.springframework.stereotype.Component

@GroupController
@PrivateController
@Component
class ImageController {

    @Action("丢")
    fun diu(@PathVar(value = 1, type = PathVar.Type.Long) qqNo: Long?, qq: Long) =
        mif.imageByUrl("https://api.kukuqaq.com/image/diu?qq=${qqNo ?: qq}")

    @Action("爬")
    fun pa(@PathVar(value = 1, type = PathVar.Type.Long) qqNo: Long?, qq: Long) =
        mif.imageByUrl("https://api.kukuqaq.com/image/pa?qq=${qqNo ?: qq}")

    @Action("嚼")
    fun jiAo(@PathVar(value = 1, type = PathVar.Type.Long) qqNo: Long?, qq: Long) =
        mif.imageByUrl("https://api.kukuqaq.com/image/jiao?qq=${qqNo ?: qq}")

    @Action("mua")
    fun mua(@PathVar(value = 1, type = PathVar.Type.Long) qqNo: Long?, qq: Long) =
        mif.imageByUrl("https://api.kukuqaq.com/image/mua?qq=${qqNo ?: qq}")

    @Action("rua")
    fun rua(@PathVar(value = 1, type = PathVar.Type.Long) qqNo: Long?, qq: Long) =
        mif.imageByUrl("https://api.kukuqaq.com/image/rua?qq=${qqNo ?: qq}")

    @Action("蹭")
    fun ce(@PathVar(value = 1, type = PathVar.Type.Long) qqNo: Long?, qq: Long) =
        mif.imageByUrl("https://api.kukuqaq.com/image/ceng?qq=${qqNo ?: qq}")

    @Action("ding")
    fun ding(@PathVar(value = 1, type = PathVar.Type.Long) qqNo: Long?, qq: Long) =
        mif.imageByUrl("https://api.kukuqaq.com/image/ding?qq=${qqNo ?: qq}")
}