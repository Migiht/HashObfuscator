package by.m1ght.transformer.pack;

import by.m1ght.Obfuscator;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.Util;
import org.objectweb.asm.tree.ClassNode;

public class Repackager extends Transformer {
    private String newPackage;

    @Override
    public void init(Obfuscator obf) {
        super.init(obf);
        newPackage = config.params.getOrDefault("package", "");
    }

    @Override
    public void transform(ClassNode node) {
        obfuscator.mapper.putClassName(node.name, Util.setNewPackage(obfuscator.mapper.mapType(node.name), newPackage));
    }

    @Override
    public TransformerType getType() {
        return TransformerType.REPACKAGER;
    }
}