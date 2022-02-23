package by.m1ght.transformer.method;

import by.m1ght.Obfuscator;
import by.m1ght.util.*;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

//TODO repair
public class StaticMethodMover extends Transformer {

    private final List<ClassNode> nodes = new ArrayList<>();

    @Override
    public void init(Obfuscator obf) {
        super.init(obf);
    }

    @Override
    public void transform(ClassNode node) {
        List<MethodNode> cache = new ArrayList<>();

        for (MethodNode method : node.methods) {
            if (canTransform(node, method) && Util.isFlag(method.access, Opcodes.ACC_STATIC) && !Util.isFlag(method.access, Opcodes.ACC_SYNTHETIC)) {
                cache.add(method);
            }
        }

        ClassNode randNode = null;
                //AsmUtil.createNode(UniqueStringGenerator.get(obfuscator.classNameGeneratorID.getAndIncrement()));
        // TODO find random
        if (!cache.isEmpty()) {
            //nodes.add(randNode);
        }
        node.methods.removeAll(cache);
        randNode.methods.addAll(cache);

        for (MethodNode method : cache) {
            //obfuscator.mapper.putMethodOwner(node.name, method.name, method.desc, randNode.name);
            LogUtil.debug("Move static method " + node.name + method.name + " to " + randNode.name);
        }
    }

    public void finish(ObjectList<ClassNode> constNodeList) {
        //obfuscator.addGenerated(nodes);
    }

    @Override
    public TransformerType getType() {
        return TransformerType.METHOD_STATIC_MOVE;
    }
}
