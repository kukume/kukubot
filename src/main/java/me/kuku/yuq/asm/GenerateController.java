package me.kuku.yuq.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ARETURN;

public class GenerateController {

	public static Class<?> test(){
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_5, ACC_PUBLIC, "me/kuku/yuq/controller/ASMController", null, "java/lang/Object", null);
		cw.visitAnnotation("Lcom/icecreamqaq/yuq/annotation/GroupController;", true);
		MethodVisitor initMv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		initMv.visitVarInsn(ALOAD, 0);
		initMv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		initMv.visitMaxs(1, 1);
		initMv.visitInsn(RETURN);
		initMv.visitEnd();
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "test", "()Ljava/lang/String;", null, new String[]{"java/io/IOException"});
		mv.visitCode();
		AnnotationVisitor av = mv.visitAnnotation("Lcom/IceCreamQAQ/Yu/annotation/Action;", true);
		av.visit("value", "sss");
		av.visitEnd();
		mv.visitLdcInsn("https://api.kuku.me");
		mv.visitMethodInsn(INVOKESTATIC, "me/kuku/yuq/utils/OkHttpUtils", "getStr", "(Ljava/lang/String;)Ljava/lang/String;", false);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
		cw.visitEnd();
		return MyClassLoader.getInstance().defineClass("me.kuku.yuq.controller.ASMController", cw.toByteArray());
	}

	public static byte[] test2(){
		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_5, ACC_PUBLIC, "me/kuku/yuq/controller/ASMController", null, "java/lang/Object", null);
		cw.visitAnnotation("Lcom/icecreamqaq/yuq/annotation/GroupController;", true);
		MethodVisitor initMv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		initMv.visitVarInsn(ALOAD, 0);
		initMv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		initMv.visitMaxs(1, 1);
		initMv.visitInsn(RETURN);
		initMv.visitEnd();
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "test", "()Ljava/lang/String;", null, new String[]{"java/io/IOException"});
		mv.visitCode();
		AnnotationVisitor av = mv.visitAnnotation("Lcom/IceCreamQAQ/Yu/annotation/Action;", true);
		av.visit("value", "sss");
		av.visitEnd();
		mv.visitLdcInsn("https://api.kuku.me");
		mv.visitMethodInsn(INVOKESTATIC, "me/kuku/yuq/utils/OkHttpUtils", "getStr", "(Ljava/lang/String;)Ljava/lang/String;", false);
		mv.visitInsn(ARETURN);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
		cw.visitEnd();
		return cw.toByteArray();
	}

}
