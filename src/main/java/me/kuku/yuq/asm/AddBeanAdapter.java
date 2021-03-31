package me.kuku.yuq.asm;

import org.objectweb.asm.*;

import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;

public class AddBeanAdapter extends ClassVisitor {

	public AddBeanAdapter(ClassVisitor cv){
		super(Opcodes.ASM5, cv);
	}

	public static byte[] asm(String className){
		try {
			ClassReader cr = new ClassReader(className);
			ClassWriter cw = new ClassWriter(cr, 0);
			AddBeanAdapter addBeanAdapter = new AddBeanAdapter(cw);
			cr.accept(addBeanAdapter, 0);
			return cw.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
		if ("<init>".equals(name)){
			mv = new AddBeanAdapterMethodAdapter(mv);
		}
		return mv;
	}


	@SuppressWarnings("InnerClassMayBeStatic")
	class AddBeanAdapterMethodAdapter extends MethodVisitor{

		public AddBeanAdapterMethodAdapter(MethodVisitor methodVisitor) {
			super(Opcodes.ASM5, methodVisitor);
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode >= IRETURN && opcode <= RETURN){
//				super.visitVarInsn(ALOAD, 4);
//				super.visitMethodInsn(INVOKESTATIC, "me/kuku/yuq/asm/GenerateController", "test", "()Ljava/lang/Class;", false);
//				super.visitMethodInsn(INVOKEVIRTUAL, "com/IceCreamQAQ/Yu/di/YuContext", "register",
//						"(Ljava/lang/Class;)V", false);
//				super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//				super.visitLdcInsn("已通过asm");
//				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
			}
			super.visitInsn(opcode);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			super.visitMaxs(maxStack + 1, maxLocals);
		}
	}
}
