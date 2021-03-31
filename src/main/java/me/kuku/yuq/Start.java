package me.kuku.yuq;

import com.IceCreamQAQ.Yu.hook.YuHook;
import com.IceCreamQAQ.Yu.loader.AppClassloader;
import com.IceCreamQAQ.Yu.util.IO;
import me.kuku.yuq.asm.AddBeanAdapter;
import me.kuku.yuq.asm.GenerateController;
import me.kuku.yuq.asm.MyClassLoader;
import me.kuku.yuq.asm.YuQStarterAdapter;
import me.kuku.yuq.utils.OkHttpUtils;
import org.objectweb.asm.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Start {
    @SuppressWarnings("ResultOfMethodCallIgnored")
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
                byte[] bytes = OkHttpUtils.downloadBytes("https://file.kuku.me/kuku-bot/YuQ.properties");
                IO.writeFile(yuqFile, bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YuHook.putMatchHookItem("me.kuku.yuq.logic.impl.BaiduAILogicImpl.*",
                "me.kuku.yuq.aop.BaiduAIAop");
        AppClassloader.registerTransformerList("com.IceCreamQAQ.Yu.web.WebClassTransformer");
        try {
            ClassReader cr = new ClassReader("com.icecreamqaq.yuq.YuQStarter");
            ClassWriter cw = new ClassWriter(0);
            cr.accept(cw, 0);

            ClassReader ccr = new ClassReader("com.icecreamqaq.yuq.YuQStarter$Companion");
            ClassWriter ccw = new ClassWriter(ccr, 0);
            YuQStarterAdapter ccc = new YuQStarterAdapter(ccw);
            ccr.accept(ccc, 0);

            ClassReader appCr = new ClassReader("com.IceCreamQAQ.Yu.DefaultApp");
            ClassWriter appCw = new ClassWriter(appCr, 0);
            AddBeanAdapter addBeanAdapter = new AddBeanAdapter(appCw);
            appCr.accept(addBeanAdapter, 0);

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