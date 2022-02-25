package by.m1ght.transformer;

import by.m1ght.Obfuscator;
import by.m1ght.config.TransformerConfig;
import by.m1ght.util.LogUtil;
import by.m1ght.util.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.BitSet;

public abstract class Transformer implements Opcodes {
    public static int ACC_SKIPPED = Integer.MIN_VALUE;
    public static int ACC_LIB = Integer.MIN_VALUE;

    public int id;
    protected Obfuscator obfuscator;
    protected TransformerConfig config;

    public void init(Obfuscator obf, int id) {
        LogUtil.log(LogUtil.Level.DEBUG, "Init " + this.getClass().getSimpleName());
        this.obfuscator = obf;
        this.config = new TransformerConfig();
        this.id = id;
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

    public boolean canTransformClass(ClassNode node) {
        return obfuscator.excludeByNode.get(node).get(id);
    }

    protected boolean canTransformMethod(ClassNode owner, MethodNode method) {
        if (Util.noFlag(method.access, Opcodes.ACC_NATIVE) && !isTransformBaseMethod() && (method.name.charAt(0) == '<' || "main".equals(method.name))) {
            return false;
        }

        return obfuscator.excludeByNode.get(method).get(id);
    }

    public boolean canTransformField(ClassNode owner, FieldNode field) {
        return obfuscator.excludeByNode.get(field).get(id);
    }

    public void computeClassExcludes(ClassNode node) {
        BitSet bitSet = obfuscator.excludeByNode.computeIfAbsent(node, (func) -> new BitSet(32));
        bitSet.set(id, config.canTransform(node));

        for (MethodNode method : node.methods) {
            bitSet = obfuscator.excludeByNode.computeIfAbsent(method, (func) -> new BitSet(32));
            bitSet.set(id, config.canTransformMethod(node, method));
        }

        for (FieldNode field : node.fields) {
            bitSet = obfuscator.excludeByNode.computeIfAbsent(field, (func) -> new BitSet(32));
            bitSet.set(id, config.canTransformField(node, field));
        }
    }
}