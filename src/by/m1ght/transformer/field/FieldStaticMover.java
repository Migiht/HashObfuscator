package by.m1ght.transformer.field;

import by.m1ght.util.AsmUtil;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.UniqueStringGenerator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class FieldStaticMover extends Transformer {
    private final List<ClassNode> nodes = new ArrayList<>();

    @Override
    public boolean isTransformGenerated() {
        return false;
    }

    @Override
    public void transform(ClassNode node) {
        List<FieldNode> cache = new ArrayList<>();

        for (FieldNode field : node.fields) {
            if (canTransform(node, field)) {
                cache.add(field);
            }
        }
        node.fields.removeAll(cache);


        ClassNode newClassNode = AsmUtil.createNode(UniqueStringGenerator.get(obfuscator.classNameGeneratorID.getAndIncrement()));

        MethodNode oldMethod = AsmUtil.getMethodNode(node, "<clinit>");

        if (oldMethod != null) {
            InsnList copy = new InsnList();
            for (AbstractInsnNode instruction : oldMethod.instructions) {
                copy.add(instruction);
            }
            node.methods.remove(oldMethod);

            MethodNode newMethod = AsmUtil.getMethodNode(newClassNode, "<clinit>");
            if (newMethod == null) {
                newMethod = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
            }
            newClassNode.methods.remove(newMethod);

            newMethod.instructions.add(copy);
            newClassNode.methods.add(newMethod);
        }

        newClassNode.fields.addAll(cache);

        for (FieldNode field : cache) {
            //obfuscator.mapper.putFieldOwner(node.name, field.name, field.desc, newClassNode.name);
        }

        if (!cache.isEmpty()) {
            //nodes.add(newClassNode);
        }
    }

    @Override
    public void finish(ObjectList<ClassNode> constNodeList) {
        //obfuscator.addGenerated(nodes);
    }

    @Override
    public boolean canTransform(ClassNode owner, FieldNode field) {
        return super.canTransform(owner, field) &&
                (field.access & Opcodes.ACC_PUBLIC) != 0 &&
                (field.access & Opcodes.ACC_STATIC) != 0;
    }

    @Override
    public TransformerType getType() {
        return TransformerType.FIELD_STATIC_MOVE;
    }
}