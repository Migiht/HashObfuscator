package by.m1ght.transformer.classnode;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import org.objectweb.asm.tree.ClassNode;

public class InnerClassRemover extends Transformer {
    @Override
    public void transform(ClassNode node) {
        node.innerClasses.clear();
        node.outerClass = null;
        node.outerMethod = null;
        node.outerMethodDesc = null;
    }

    @Override
    public TransformerType getType() {
        return TransformerType.INNER_REMOVER;
    }
}
