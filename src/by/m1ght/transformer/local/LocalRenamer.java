package by.m1ght.transformer.local;

import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.LogUtil;
import by.m1ght.util.UniqueStringGenerator;
import org.objectweb.asm.tree.*;

public class LocalRenamer extends Transformer {

    @Override
    public void transform(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (canTransformMethod(node, method) && method.localVariables != null) {
                int generatorID = 0;
                for (LocalVariableNode localVariable : method.localVariables) {
                    localVariable.name = UniqueStringGenerator.get(generatorID++);
                    LogUtil.debug(localVariable.name);
                }
            }
        }
    }

    @Override
    protected boolean isTransformBaseMethod() {
        return true;
    }

    @Override
    public TransformerType getType() {
        return TransformerType.LOCAL_RENAME;
    }
}