package by.m1ght.transformer.other;

import by.m1ght.Obfuscator;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.AsmUtil;
import by.m1ght.util.UniqueStringGenerator;
import by.m1ght.util.Util;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class StringRandomizer extends Transformer {
    private long secret = Long.MAX_VALUE;

    @Override
    public void init(Obfuscator obf) {
        super.init(obf);

        secret = Long.parseLong(config.params.getOrDefault("SECRET", String.valueOf(Long.MAX_VALUE)));
    }

    @Override
    public void transform(ClassNode node) {
        for (MethodNode method : node.methods) {
            AsmUtil.processLDCString(method, (ldc, name) -> {
                if (name.startsWith("rand.str")) {
                    ldc.cst = UniqueStringGenerator.get(Util.hash((String) ldc.cst) ^ secret);
                }
            });
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.STRING_RANDOMIZE;
    }
}
