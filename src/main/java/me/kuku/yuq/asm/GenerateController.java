package me.kuku.yuq.asm;

import com.alibaba.fastjson.JSON;
import me.kuku.yuq.pojo.ActionPojo;
import me.kuku.yuq.utils.IOUtils;
import me.kuku.yuq.utils.OkHttpUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ARETURN;

public class GenerateController {
	/**
	 * class ASMController{
	 *  @Action("sss")
	 *  public String test() throw IOException{
	 *          return OkHttpUtils.getStr("https://api.kuku.me");
	 *  }
	 * }
	 */
	public static byte[] generate(){
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_5, ACC_PUBLIC, "me/kuku/yuq/controller/ASMController", null, "java/lang/Object", null);
		cw.visitAnnotation("Lcom/icecreamqaq/yuq/annotation/GroupController;", true);
		MethodVisitor initMv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		initMv.visitVarInsn(ALOAD, 0);
		initMv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		initMv.visitMaxs(1, 1);
		initMv.visitInsn(RETURN);
		initMv.visitEnd();
		List<ActionPojo> list = new ArrayList<>();
		FileInputStream fis = null;
		try {
			Properties properties = new Properties();
			fis = new FileInputStream("conf/YuQ.properties");
			properties.load(fis);
			Object urlStr = properties.get("YuQ.Mirai.bot.command.url");
			if (urlStr == null){
				urlStr = "https://api.kuku.me/bot";
			}
			String[] arr = urlStr.toString().split("\\|");
			for (String url: arr){
				String str = OkHttpUtils.getStr(url);
				list.addAll(JSON.parseArray(str, ActionPojo.class));
			}
		} catch (Exception ignore) {
		} finally {
			IOUtils.close(fis);
		}
		if (list.size() != 0) {
			for (ActionPojo pojo : list) {
				List<String> params = pojo.getParams();
				StringBuilder sb = new StringBuilder();
				int size = 0;
				if (params != null && params.size() != 0){
					sb.append("Ljava/lang/String;");
					size++;
				}
				MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, pojo.getName(), "(" + sb.toString() + ")Ljava/lang/String;", null, new String[]{"java/lang/Exception"});
				mv.visitCode();

				AnnotationVisitor qMsgAv = mv.visitAnnotation("Lcom/icecreamqaq/yuq/annotation/QMsg;", true);
				qMsgAv.visit("at", true);
				qMsgAv.visit("atNewLine", true);
				qMsgAv.visitEnd();
				AnnotationVisitor av = mv.visitAnnotation("Lcom/IceCreamQAQ/Yu/annotation/Action;", true);
				av.visit("value", pojo.getAction());
				av.visitEnd();

				if (params != null) {
					for (int i = 0; i < params.size(); i++){
						String param = params.get(i);
						AnnotationVisitor pav = mv.visitParameterAnnotation(i, "Ljavax/inject/Named;", true);
						pav.visit("value", param);
						pav.visitEnd();
					}
				}

				Label start = new Label();
				mv.visitLabel(start);

				mv.visitLdcInsn(pojo.getUrl());

				if (params != null && params.size() != 0) {
					mv.visitLdcInsn(params.size());
					mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
					mv.visitInsn(DUP);
					for (int i = 0; i < params.size(); i++) {
						mv.visitLdcInsn(i);
						mv.visitVarInsn(ALOAD, i + 1);
						mv.visitInsn(AASTORE);
						if (i != params.size() - 1)
							mv.visitInsn(DUP);
					}
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "format", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;", false);
				}
				mv.visitLdcInsn("Accept");
				mv.visitLdcInsn("text/plain");
				mv.visitMethodInsn(INVOKESTATIC, "me/kuku/yuq/utils/OkHttpUtils", "addSingleHeader",
						"(Ljava/lang/String;Ljava/lang/String;)Lokhttp3/Headers;", false);
				mv.visitMethodInsn(INVOKESTATIC, "me/kuku/yuq/utils/OkHttpUtils", "getStr", "(Ljava/lang/String;Lokhttp3/Headers;)Ljava/lang/String;", false);
				mv.visitInsn(ARETURN);
				Label end = new Label();
				mv.visitLabel(end);
				mv.visitLocalVariable("this", "Lme/kuku/yuq/controller/ASMController;", null, start, end, 0);
				if (params != null) {
					for (int i = 0; i < params.size(); i++) {
						String param = params.get(i);
						mv.visitLocalVariable(param, "Ljava/lang/String;", null, start, end, i + 1);
					}
				}
				mv.visitMaxs(5, 2 + size);
				mv.visitEnd();
			}
		}
		cw.visitEnd();
		return cw.toByteArray();
	}

}
