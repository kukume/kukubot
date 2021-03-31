package me.kuku.yuq.asm;

import com.IceCreamQAQ.Yu.loader.AppClassloader;

public class MyClassLoader extends ClassLoader {
	private static MyClassLoader classLoader;

	private MyClassLoader(){ }

	public static MyClassLoader getInstance(){
		if (classLoader == null){
			synchronized (MyClassLoader.class){
				if (classLoader == null){
					try {
						classLoader = new MyClassLoader();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return classLoader;
	}

	public Class<?> defineClass(String name, byte[] b) {
		return defineClass(name, b, 0, b.length);
	}
}
