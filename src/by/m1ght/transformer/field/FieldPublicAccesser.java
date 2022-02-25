package by.m1ght.transformer.field;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.AsmUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

public class FieldPublicAccesser extends Transformer {
    @Override
    public void transform(ClassNode node) {
        for (FieldNode field : node.fields) {
            if (canTransformField(node, field)) {
                field.access = AsmUtil.changeToPublic(field.access);
                if (field.value == null) {
                    field.access = AsmUtil.changeToNotFinal(field.access);
                }
            }
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.FIELD_PUBLIC;
    }
}
