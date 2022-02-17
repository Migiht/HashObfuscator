package by.m1ght.transformer.classnode;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.AsmUtil;
import org.objectweb.asm.tree.ClassNode;

public class ClassPublicAccesser extends Transformer {

    @Override
    public void transform(ClassNode node) {
        node.access = AsmUtil.changeToPublic(node.access);
        node.access = AsmUtil.changeToNotFinal(node.access);
    }

    @Override
    public TransformerType getType() {
        return TransformerType.CLASS_PUBLIC;
    }
}