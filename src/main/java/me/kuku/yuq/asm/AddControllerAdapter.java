package me.kuku.yuq.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.io.IOException;

import static jdk.internal.org.objectweb.asm.Opcodes.*;

public class AddControllerAdapter extends ClassVisitor {
	public AddControllerAdapter(ClassVisitor classVisitor) {
		super(ASM5, classVisitor);
	}

	public static byte[] asm(String className){
		try {
			ClassReader cr = new ClassReader(className);
			ClassWriter cw = new ClassWriter(cr, 0);
			AddControllerAdapter addControllerAdapter = new AddControllerAdapter(cw);
			cr.accept(addControllerAdapter, 0);
			return cw.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
		if (name.equals("load")){
			mv = new AddControllerMethodAdapter(mv);
		}
		return mv;
	}

	@SuppressWarnings("InnerClassMayBeStatic")
	class AddControllerMethodAdapter extends MethodVisitor{
		private boolean status = false;

		public AddControllerMethodAdapter(MethodVisitor methodVisitor) {
			super(ASM5, methodVisitor);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
			if (owner.equals("java/util/HashMap") && name.equals("<init>")){
				status = true;
			}
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
			super.visitVarInsn(opcode, var);
			if (status){
				super.visitVarInsn(ALOAD, 2);
				super.visitVarInsn(ALOAD, 0);
				super.visitMethodInsn(INVOKEVIRTUAL, "com/IceCreamQAQ/Yu/loader/AppLoader", "getAppClassloader",
						"()Ljava/lang/ClassLoader;", false);
//				super.visitTypeInsn(CHECKCAST, "com/IceCreamQAQ/Yu/loader/AppClassloader");
				super.visitLdcInsn("me.kuku.yuq.controller.ASMController");

				super.visitMethodInsn(INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass",
						"(Ljava/lang/String;)Ljava/lang/Class;", false);
				super.visitVarInsn(ASTORE, 4);
				super.visitLdcInsn("me.kuku.yuq.controller.ASMController");
				super.visitVarInsn(ALOAD, 4);
				super.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put",
						"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
				super.visitInsn(POP);
				status = false;
			}
		}
	}


}
