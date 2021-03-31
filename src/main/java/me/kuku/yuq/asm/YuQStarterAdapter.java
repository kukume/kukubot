package me.kuku.yuq.asm;

import me.kuku.yuq.utils.RSAUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class YuQStarterAdapter extends ClassVisitor {
	public YuQStarterAdapter(ClassVisitor cv) {
		super(Opcodes.ASM5, cv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
		if (name.equals("start")) {
			mv = new YuQStarterMethodAdapter(mv);
		}
		return mv;
	}

	@SuppressWarnings("InnerClassMayBeStatic")
	class YuQStarterMethodAdapter extends MethodVisitor{
		public YuQStarterMethodAdapter(MethodVisitor mv){
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
		public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
			super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
//			if (name.equals("<init>") && owner.equals("com/IceCreamQAQ/Yu/loader/AppClassloader")){
//				super.visitTypeInsn(Opcodes.NEW, "org/objectweb/asm/ClassReader");
//				super.visitInsn(Opcodes.DUP);
//				super.visitLdcInsn("com.IceCreamQAQ.Yu.DefaultApp");
//				super.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/objectweb/asm/ClassReader", "<init>", "(Ljava/lang/String;)V", false);
//				super.visitVarInsn(Opcodes.ASTORE, 5);
//				super.visitTypeInsn(Opcodes.NEW, "org/objectweb/asm/ClassWriter");
//				super.visitInsn(Opcodes.DUP);
//				super.visitVarInsn(Opcodes.ALOAD, 5);
//				super.visitInsn(Opcodes.ICONST_0);
//				super.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/objectweb/asm/ClassWriter", "<init>", "(Lorg/objectweb/asm/ClassReader;I)V", false);
//				super.visitVarInsn(Opcodes.ASTORE, 6);
//				super.visitTypeInsn(Opcodes.NEW, "me/kuku/yuq/asm/AddBeanAdapter");
//				super.visitInsn(Opcodes.DUP);
//				super.visitVarInsn(Opcodes.ALOAD, 6);
//				super.visitMethodInsn(Opcodes.INVOKESPECIAL, "me/kuku/yuq/asm/AddBeanAdapter", "<init>", "(Lorg/objectweb/asm/ClassVisitor;)V", false);
//				super.visitVarInsn(Opcodes.ASTORE, 7);
//				super.visitVarInsn(Opcodes.ALOAD, 5);
//				super.visitVarInsn(Opcodes.ALOAD, 7);
//				super.visitInsn(Opcodes.ICONST_0);
//				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/objectweb/asm/ClassReader", "accept", "(Lorg/objectweb/asm/ClassVisitor;I)V", false);
//				super.visitVarInsn(Opcodes.ALOAD, 6);
//				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/objectweb/asm/ClassWriter", "toByteArray", "()[B", false);
//				super.visitVarInsn(Opcodes.ASTORE, 8);
//				super.visitVarInsn(Opcodes.ALOAD, 4);
//				super.visitLdcInsn("com.IceCreamQAQ.Yu.DefaultApp");
//				super.visitVarInsn(Opcodes.ALOAD, 8);
//				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/IceCreamQAQ/Yu/loader/AppClassloader", "define",
//						"(Ljava/lang/String;[B)Ljava/lang/Class;", false);
////				super.visitInsn(Opcodes.POP);
//			}
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			super.visitMaxs(maxStack + 10, maxLocals + 10);
		}
	}
}