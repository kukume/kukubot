package me.kuku.yuq;

import com.IceCreamQAQ.Yu.hook.HookItem;
import com.IceCreamQAQ.Yu.hook.YuHook;
import com.IceCreamQAQ.Yu.loader.AppClassloader;
import com.icecreamqaq.yuq.YuQStarter;

import java.util.ArrayList;
import java.util.List;

public class Start {
	public static void main(String[] args) {
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
