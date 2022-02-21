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
    public void visitLdcInsn(Object value) {
        super.visitLdcInsn(((ObfRemapper) remapper).mapLDC(value));
    }
}