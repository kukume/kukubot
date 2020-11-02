@file:Suppress("unused")

package me.kuku.yuq.controller

import com.IceCreamQAQ.Yu.annotation.Action
import com.IceCreamQAQ.Yu.annotation.Synonym
import com.icecreamqaq.yuq.annotation.GroupController
import me.kuku.yuq.utils.removeSuffixLine

@GroupController
class MenuController {

    @Action("help")
    fun help() = menu(MenuController::class.java)

    @Action("tool")
    fun tool() = menu(ToolController::class.java)

    @Action("bilibili")
    fun bl() = menu(BiliBiliLoginController::class.java) + "\n" + menu(BiliBiliController::class.java)

    @Action("bot")
    fun bot() = menu(BotController::class.java)

    @Action("manage")
    fun manage() = menu(ManageNotController::class.java) + "\n" + menu(ManageOwnerController::class.java) +
            "\n" + menu(ManageAdminController::class.java)

    @Action("motion")
    fun motion() = menu(MotionController::class.java) + "\n" + menu(BindStepController::class.java)

    @Action("wy")
    fun wy() = menu(NeTeaseController::class.java) + "\n" + menu(BindNeTeaseController::class.java)

    @Action("qq")
    fun qq() = menu(QQController::class.java) + "\n" + menu(BindQQController::class.java) +
            "\n" + menu(QQJobController::class.java)

    @Action("setting")
    fun setting() = menu(SettingController::class.java)

    @Action("wb")
    fun wb() = menu(WeiboNotController::class.java) + "\n" + menu(WeiboController::class.java)

    fun menu(clazz: Class<*>): String{
        val sb = StringBuilder()
        val methods = clazz.methods
        for (method in methods){
            val action = method.getAnnotation(Action::class.java)
            if (action != null){
                sb.appendLine(action.value)
            }
            val synonym = method.getAnnotation(Synonym::class.java)
            if (synonym != null){
                val arr = synonym.value
                for (str in arr){
                    sb.appendLine(str)
                }
            }
        }
        return sb.removeSuffixLine().toString()
    }
}