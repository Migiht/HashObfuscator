package by.m1ght.transformer.method;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MethodShuffler extends Transformer {
    @Override
    public void transform(ClassNode node) {
        if (node.methods.size() > 1) {
            List<MethodNode> methods = node.methods;
            for (int i = 0; i < methods.size(); i++) {
                MethodNode method = methods.get(i);
                if (canTransform(node, method)) {
                    int swapIndex = ThreadLocalRandom.current().nextInt(node.methods.size());
                    MethodNode swap = node.methods.get(swapIndex);
                    if (canTransform(node, swap)) {
                        Collections.swap(methods, i, swapIndex);
                    }
                }
            }
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.METHOD_SHUFFLE;
    }
}
