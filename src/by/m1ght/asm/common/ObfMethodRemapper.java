package by.m1ght.asm.common;

import by.m1ght.Obfuscator;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

public class ObfMethodRemapper extends MethodRemapper {

    public ObfMethodRemapper(MethodVisitor methodVisitor, Remapper remapper) {
        super(methodVisitor, remapper);
    }

    protected ObfMethodRemapper(int api, MethodVisitor methodVisitor, Remapper remapper) {
        super(api, methodVisitor, remapper);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitMethodInsn(int opcodeAndSource, String owner, String name, String descriptor, boolean isInterface) {
        super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(((ObfRemapper) remapper).mapLDC(value));
    }
}