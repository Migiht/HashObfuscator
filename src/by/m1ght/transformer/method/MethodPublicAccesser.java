package by.m1ght.transformer.method;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.AsmUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodPublicAccesser extends Transformer {
    @Override
    public void transform(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (canTransformMethod(node, method)) {
                method.access = AsmUtil.changeToPublic(method.access);
                method.access = AsmUtil.changeToNotFinal(method.access);
            }
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.METHOD_PUBLIC;
    }
}
