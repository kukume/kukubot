package me.kuku.yuq.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.IceCreamQAQ.Yu.util.IO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

@JobCenter
public class BackupDeviceInfoJob {
    @Cron("1h")
    public void backUp(){
        File confFile = new File("conf");
        if (confFile.exists()){
            String deviceName = "device.json";
            File rootDeviceFile = new File(deviceName);
            File confDeviceFile = new File("conf/" + deviceName);
            if (rootDeviceFile.exists()){
                try {
                    IO.writeFile(confDeviceFile, IO.read(new FileInputStream(rootDeviceFile), true));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
