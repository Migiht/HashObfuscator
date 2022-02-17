package by.m1ght.transformer.other;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Iterator;

public class DebugRemover extends Transformer {

    @Override
    public void transform(ClassNode node) {
        node.sourceDebug = null;
        node.sourceFile = null;

        for (MethodNode method : node.methods) {
            Iterator<AbstractInsnNode> instructions = method.instructions.iterator();
            while (instructions.hasNext()) {
                // If LineNumberNode
                if (instructions.next().getType() == AbstractInsnNode.LINE) {
                    instructions.remove();
                }
            }
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.DEBUG_CLEAR;
    }
}
