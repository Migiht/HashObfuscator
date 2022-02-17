package by.m1ght.transformer.classnode;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.UniqueStringGenerator;
import by.m1ght.util.Util;
import org.objectweb.asm.tree.ClassNode;

public class ClassRenamer extends Transformer {

    @Override
    public boolean isTransformGenerated() {
        return false;
    }

    @Override
    public void transform(ClassNode node) {
        obfuscator.mapper.putClassName(node.name, Util.setNewName(obfuscator.mapper.mapType(node.name), UniqueStringGenerator.get(obfuscator.classNameGeneratorID.getAndIncrement())));
    }

    @Override
    public TransformerType getType()
    {
        return TransformerType.CLASS_RENAME;
    }
}
