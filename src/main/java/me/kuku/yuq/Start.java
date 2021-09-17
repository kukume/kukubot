package me.kuku.yuq;

import com.IceCreamQAQ.Yu.hook.HookItem;
import com.IceCreamQAQ.Yu.hook.YuHook;
import com.IceCreamQAQ.Yu.loader.AppClassloader;
import com.IceCreamQAQ.Yu.util.IO;
import com.icecreamqaq.yuq.YuQStarter;
import me.kuku.utils.OkHttpUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Start {
	public static void main(String[] args) {
		// 从conf文件夹拉设备信息到根目录
		String deviceName = "device.json";
		File confDeviceFile = new File("conf" + File.separator + deviceName);
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
		File yuqFile = new File("conf" + File.separator + "YuQ.properties");
		if (!yuqFile.exists()){
			try {
				byte[] bytes = OkHttpUtils.downloadBytes("https://static.kukuqaq.com/YuQ.properties");
				IO.writeFile(yuqFile, bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		List<String> packageName = new ArrayList<>();
		packageName.add("me.kuku.yuq.entity.GroupEntity");
		packageName.add("me.kuku.yuq.entity.QqEntity");
		packageName.add("com.alibaba.fastjson.JSONObject");
		packageName.add("com.alibaba.fastjson.JSONArray");
		AppClassloader.registerBackList(packageName);
		YuHook.put(new HookItem("org.hibernate.Version", "initVersion", "com.icecreamqaq.yudb.HibernateVersionHook"));
		YuQStarter.start();
	}
}
