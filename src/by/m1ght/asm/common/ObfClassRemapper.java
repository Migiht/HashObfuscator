package by.m1ght.asm.common;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

public class ObfClassRemapper extends ClassRemapper {

    public ObfClassRemapper(ClassVisitor classVisitor, Remapper remapper) {
        super(classVisitor, remapper);
    }

    protected ObfClassRemapper(int api, ClassVisitor classVisitor, Remapper remapper) {
        super(api, classVisitor, remapper);
    }

    public ObfClassRemapper setVisitor(ClassVisitor visitor) {
        this.cv = visitor;
        return this;
    }

    @Override
    protected MethodVisitor createMethodRemapper(MethodVisitor methodVisitor) {
        return new ObfMethodRemapper(this.api, methodVisitor, this.remapper);
    }
}
