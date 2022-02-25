package by.m1ght.transformer.field;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.Util;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FieldShuffler extends Transformer {
    @Override
    public void transform(ClassNode node) {
        if (node.fields.size() > 1 && !Util.isFlag(node.access, Opcodes.ACC_ENUM)) {
            List<FieldNode> methods = node.fields;
            for (int i = 0; i < methods.size(); i++) {
                FieldNode method = methods.get(i);
                if (canTransformField(node, method)) {
                    int swapIndex = ThreadLocalRandom.current().nextInt(node.fields.size());
                    FieldNode swap = node.fields.get(swapIndex);
                    if (canTransformField(node, swap)) {
                        Collections.swap(methods, i, swapIndex);
                    }
                }
            }
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.FIELD_SHUFFLE;
    }
}
