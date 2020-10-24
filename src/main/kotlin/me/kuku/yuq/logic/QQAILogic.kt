package me.kuku.yuq.logic

import com.IceCreamQAQ.Yu.annotation.AutoBind
import me.kuku.yuq.pojo.CommonResult

@AutoBind
interface QQAILogic {
    fun pornIdentification(imageUrl: String): Boolean
    fun generalOCR(imageUrl: String): String
    fun textChat(question: String, session: String): String
    fun echoSpeechRecognition(url: String): String
    fun aiLabSpeechRecognition(url: String): String
    fun voiceSynthesis(text: String): CommonResult<ByteArray>
}