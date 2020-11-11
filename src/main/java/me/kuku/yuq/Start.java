package me.kuku.yuq;

import com.IceCreamQAQ.Yu.loader.AppClassloader;
import com.IceCreamQAQ.Yu.util.IO;
import com.icecreamqaq.yuq.YuQStarter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Start {
    public static void main(String[] args) {
        String deviceName = "device.json";
        File confDeviceFile = new File("conf/" + deviceName);
        File rootDeviceFile = new File(deviceName);
        if (confDeviceFile.exists() && !rootDeviceFile.exists()){
            try {
                IO.writeFile(rootDeviceFile, IO.read(new FileInputStream(confDeviceFile), true));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        AppClassloader.registerTransformerList("com.IceCreamQAQ.Yu.web.WebClassTransformer");
        YuQStarter.start();
    }
}
