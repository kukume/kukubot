package me.kuku.yuq;

import com.IceCreamQAQ.Yu.loader.AppClassloader;
import com.IceCreamQAQ.Yu.util.IO;
import me.kuku.yuq.asm.MyClassLoader;
import me.kuku.yuq.asm.YuQStarterAdapter;
import me.kuku.yuq.utils.OkHttpUtils;
import org.objectweb.asm.*;

import java.io.*;
import java.lang.reflect.Method;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Start {
    public static void main(String[] args) {
        // 从conf文件夹拉设备信息到根目录
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
        // 如果没有配置文件，下载配置文件
        File confFile = new File("conf");
        if (!confFile.exists()) confFile.mkdir();
        File yuqFile = new File("conf/YuQ.properties");
        if (!yuqFile.exists()){
            try {
                byte[] bytes = OkHttpUtils.downloadBytes("https://vkceyugu.cdn.bspapp.com/VKCEYUGU-ba222f61-ee83-431d-bf9f-7e6216a8cf41/0b84f939-3d10-45d6-a453-8bbb6828742f.properties");
                IO.writeFile(yuqFile, bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        AppClassloader.registerTransformerList("com.IceCreamQAQ.Yu.web.WebClassTransformer");
        try {
            ClassReader cr = new ClassReader("com.icecreamqaq.yuq.YuQStarter");
            ClassWriter cw = new ClassWriter(0);
            cr.accept(cw, 0);

            ClassReader ccr = new ClassReader("com.icecreamqaq.yuq.YuQStarter$Companion");
            ClassWriter ccw = new ClassWriter(ccr, 0);
            YuQStarterAdapter ccc = new YuQStarterAdapter(ccw);
            ccr.accept(ccc, 0);

            MyClassLoader classLoader = MyClassLoader.getInstance();
            Class<?> clazz = classLoader.defineClass("com.icecreamqaq.yuq.YuQStarter", cw.toByteArray());
            classLoader.defineClass("com.icecreamqaq.yuq.YuQStarter$Companion", ccw.toByteArray());
            Method startMethod = clazz.getDeclaredMethod("start");
            startMethod.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}