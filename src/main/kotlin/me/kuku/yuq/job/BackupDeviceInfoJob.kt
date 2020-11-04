@file:Suppress("unused")

package me.kuku.yuq.job

import com.IceCreamQAQ.Yu.annotation.Cron
import com.IceCreamQAQ.Yu.annotation.JobCenter
import com.IceCreamQAQ.Yu.util.IO
import java.io.File

@JobCenter
class BackupDeviceInfoJob {

    @Cron("1h")
    fun backUp(){
        val confFile = File("conf")
        if (confFile.exists()){
            val deviceName = "device.json"
            val rootDeviceFile = File(deviceName)
            val confDeviceFile = File("conf/$deviceName")
            if (rootDeviceFile.exists()){
                IO.writeFile(confDeviceFile, rootDeviceFile.readBytes())
            }
        }
    }
}