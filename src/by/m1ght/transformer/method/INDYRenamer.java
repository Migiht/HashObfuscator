package by.m1ght.transformer.method;

import by.m1ght.Obfuscator;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.AsmUtil;
import by.m1ght.util.LogUtil;
import by.m1ght.util.Util;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class INDYRenamer extends Transformer {

    @Override
    public void transform(ClassNode node) {
        for (MethodNode methodNode : node.methods) {
            for (AbstractInsnNode instruction : methodNode.instructions) {
                if (instruction.getType() == AbstractInsnNode.INVOKE_DYNAMIC_INSN) {
                    InvokeDynamicInsnNode indyInsn = (InvokeDynamicInsnNode) instruction;
                    String className = AsmUtil.getReturnType(indyInsn.desc);
                    ClassNode classNode = obfuscator.nodeByName.get(className);

                    if (classNode != null && Util.noFlag(classNode.access, ACC_LIB)) {
                        MethodNode findMethod = AsmUtil.getMethodNode(classNode, indyInsn.name);
                        if (findMethod != null) {
                            if (Util.noFlag(findMethod.access, ACC_SKIPPED)) {
                                obfuscator.mapper.methodHasher.putINDY(className, findMethod.name, findMethod.desc, indyInsn);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.INDY_RENAME;
    }
}
