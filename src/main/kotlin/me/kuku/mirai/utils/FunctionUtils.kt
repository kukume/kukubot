package me.kuku.mirai.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

suspend fun ffmpeg(command: String) {
    val runtime = Runtime.getRuntime()
    val process = withContext(Dispatchers.IO) {
        runtime.exec("${if (System.getProperty("os.name").contains("Windows")) "cmd /C " else ""}$command")
    }
    thread(true) {
        BufferedReader(InputStreamReader(process.inputStream)).use { br ->
            while (true) {
                br.readLine() ?: break
            }
        }
    }
    thread(true) {
        BufferedReader(InputStreamReader(process.errorStream)).use { br ->
            while (true) {
                br.readLine() ?: break
            }
        }
    }
    withContext(Dispatchers.IO) {
        process.waitFor()
    }
}

