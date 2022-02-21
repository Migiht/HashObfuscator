package by.m1ght.transformer.other;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.AsmUtil;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class LDCCache extends Transformer {

    @Override
    public void transform(ClassNode node) {
        for (MethodNode method : node.methods) {
            AsmUtil.processLDCString(method, (ldc, name) -> obfuscator.mapper.putLDC(name));
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.STRING_REPLACE;
    }
}
