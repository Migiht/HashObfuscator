package by.m1ght.transformer.field;

import by.m1ght.Obfuscator;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.AsmUtil;
import by.m1ght.util.Util;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Objects;

public class FieldRenamer extends Transformer {

    @Override
    public void init(Obfuscator obf) {
        super.init(obf);
        obf.mapper.fieldHasher.setType(config.params.get("generator_type"));
    }

    @Override
    public void transform(ClassNode node) {
        for (FieldNode field : node.fields) {
            if (canTransform(node, field) && Util.noFlag(field.access, ACC_SYNTHETIC)) {
                obfuscator.mapper.fieldHasher.put(node.name, field.name, field.desc);
            } else {
                field.access = Util.setFlag(field.access, ACC_SKIPPED);
            }
        }

        if (!node.fields.isEmpty() && Util.isFlag(node.access, ACC_ENUM)) {
            MethodNode clinit = AsmUtil.getMethodNode(node, "<clinit>");
            if (clinit != null) {
                AsmUtil.processLDCString(clinit, (ldc, name) -> {
                    for (FieldNode field : node.fields) {
                        if (Objects.equals(name, field.name)) {
                            String newName = obfuscator.mapper.mapFieldName(node.name, field.name, field.desc);
                            if (!Objects.equals(newName, field.name)) {
                                ldc.cst = newName;
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.FIELD_RENAME;
    }

    public static class SuperRenamer extends Transformer {

        @Override
        public TransformerType getType() {
            return TransformerType.FIELD_RENAME;
        }

        @Override
        public void transform(ClassNode node) {
            ClassNode findIn = node;

            while (true) {
                findIn = obfuscator.nodeByName.get(findIn.superName);

                if (findIn == null) {
                    return;
                }

                if (!findIn.fields.isEmpty() && Util.noFlag(findIn.access, ACC_LIB)) {
                    for (FieldNode field : findIn.fields) {
                        if (Util.noFlag(field.access, ACC_SKIPPED)) {
                            obfuscator.mapper.fieldHasher.putReplaceOwner(findIn.name, node.name, field.name, field.desc);
                        }
                    }
                }
            }
        }
    }
}
