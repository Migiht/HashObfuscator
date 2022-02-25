package by.m1ght.transformer;

import by.m1ght.Obfuscator;
import by.m1ght.config.TransformerConfig;
import by.m1ght.util.LogUtil;
import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Collections;
import java.util.List;

public abstract class Transformer implements Opcodes {
    public static int ACC_SKIPPED = Integer.MIN_VALUE;
    public static int ACC_LIB = Integer.MIN_VALUE;

    protected Obfuscator obfuscator;
    protected TransformerConfig config;

    public void init(Obfuscator obf) {
        LogUtil.log(LogUtil.Level.DEBUG, "Init " + this.getClass().getSimpleName());
        this.obfuscator = obf;
        this.config = new TransformerConfig();
    }

    public abstract void transform(ClassNode node);

    public TransformerType getType() {
        return TransformerType.GLOBAL;
    }

    public void applyConfig(TransformerConfig config) {
        this.config.applyConfig(config);
    }

    protected boolean isTransformBaseMethod() {
        return false;
    }

    protected boolean canTransform(ClassNode owner, MethodNode method) {
        if (!isTransformBaseMethod() && (method.name.charAt(0) == '<' || "main".equals(method.name))) {
            return false;
        }
        return Util.noFlag(method.access, Opcodes.ACC_NATIVE) && config.canTransform(Util.hash(owner.name, method.name), method.access, method.visibleAnnotations);
    }

    public boolean canTransform(ClassNode node) {
        return config.canTransformPackage(node.name);
    }

    public boolean canTransform(ClassNode owner, FieldNode field) {
        return config.canTransform(Util.hash(owner.name, field.name), field.access, field.visibleAnnotations);
    }
}