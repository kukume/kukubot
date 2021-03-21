package me.kuku.yuq;

import com.IceCreamQAQ.Yu.hook.YuHook;
import com.IceCreamQAQ.Yu.loader.AppClassloader;
import com.IceCreamQAQ.Yu.util.IO;
import me.kuku.yuq.utils.OkHttpUtils;
import me.kuku.yuq.utils.RSAUtils;
import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
            MyClassAdapter ccc = new MyClassAdapter(ccw);
            ccr.accept(ccc, 0);
            MyClassLoader classLoader = new MyClassLoader();
            Class<?> clazz = classLoader.defineClass("com.icecreamqaq.yuq.YuQStarter", cw.toByteArray());
            classLoader.defineClass("com.icecreamqaq.yuq.YuQStarter$Companion", ccw.toByteArray());
            Method startMethod = clazz.getDeclaredMethod("start");
            startMethod.invoke(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MyClassAdapter extends ClassVisitor {
    public MyClassAdapter(ClassVisitor cv){
        super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (name.equals("start")){
            mv = new MyMethodAdapter(mv);
        }
        return mv;
    }
}

class MyMethodAdapter extends MethodVisitor{
    public MyMethodAdapter(MethodVisitor mv){
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitLdcInsn(Object value) {
        String str = value.toString();
        if (str.startsWith("感谢您使用 YuQ 进行开发")){
            try {
                String my =
                        "    __         __               __          __ \n" +
                                "   / /____  __/ /____  __      / /_  ____  / /_\n" +
                                "  / //_/ / / / //_/ / / /_____/ __ \\/ __ \\/ __/\n" +
                                " / ,< / /_/ / ,< / /_/ /_____/ /_/ / /_/ / /_  \n" +
                                "/_/|_|\\__,_/_/|_|\\__,_/     /_.___/\\____/\\__/  \n";
                String[] arr = RSAUtils.getRsaKey();
                if (arr == null){
                    value = "感谢您使用 YuQ 进行开发，在您使用中如果遇到任何问题，可以到 Github，Gitee 提出 issue，您也可以添加 YuQ 的开发交流群（Njk2MTI5MTI4）进行交流。";
                }else {
                    super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                    super.visitLdcInsn("如需加入YuQ开发群，请使用rsa解密群号（https://www.bejson.com/enc/rsa/），rsa秘钥如下：\n" + arr[1]);
                    super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
                    String groupNumber = new String(Base64.getDecoder().decode("Njk2MTI5MTI4"), StandardCharsets.UTF_8);
                    groupNumber = RSAUtils.encrypt(groupNumber, RSAUtils.getPublicKeyOriginal(arr[0]));
                    value = "感谢您使用 YuQ 进行开发，在您使用中如果遇到任何问题，可以到 Github（https://github.com/IceCream-Open/Rain、https://github.com/YuQWorks/YuQ）提出 issue，您也可以添加 YuQ 的开发交流群\n" +
                            "（" + groupNumber + "）\n" +
                            "进行交流。\n" +
                            my + "\n" +
                            "感谢您使用 kuku-bot，在您使用中如果遇到任何问题，可以到 Github（https://github.com/kukume/kuku-bot）提出 issue";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.visitLdcInsn(value);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 2, maxLocals);
    }
}


class MyClassLoader extends ClassLoader {
    public Class<?> defineClass(String name, byte[] b) {
        return defineClass(name, b, 0, b.length);
    }
}
