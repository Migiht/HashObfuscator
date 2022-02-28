package by.m1ght.transformer.method;

import by.m1ght.Obfuscator;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.AsmUtil;
import by.m1ght.util.LogUtil;
import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Map;

public class MethodRenamer extends Transformer {
    private final Map<String, String> superMethodCache = new Object2ObjectOpenHashMap<>();

    @Override
    public void init(Obfuscator obf) {
        super.init(obf);
        addToCache(Object.class);
        addToCache(Enum.class);
        obf.mapper.methodHasher.setType(config.params.get("generator_type"));
    }

    private void addToCache(Class<?> superClass) {
        ClassNode node = obfuscator.nodeByName.get(AsmUtil.toAsmName(superClass.getName()));
        if (node != null) {
            for (MethodNode method : node.methods) {
                superMethodCache.put(method.name, method.desc);
                LogUtil.info("Add to super cache " + method.name + method.desc);
            }
        }
    }

    @Override
    protected boolean canTransformMethod(ClassNode owner, MethodNode method) {
        if (method.desc.equals(superMethodCache.get(method.name))) {
            return false;
        }

        return super.canTransformMethod(owner, method) && (!isSuperMethod(owner, method) && !isInterfaceMethod(owner, method));
    }

    @Override
    public void transform(ClassNode node) {
        for (MethodNode method : node.methods) {
            if (canTransformMethod(node, method)) {
                obfuscator.mapper.methodHasher.put(node.name, method.name, method.desc);
            } else {
                method.access = Util.setFlag(method.access, ACC_SKIPPED);
            }
        }
    }

    protected boolean isInterfaceMethod(ClassNode classNode, MethodNode method) {
        for (String anInterface : classNode.interfaces) {
            classNode = obfuscator.nodeByName.get(anInterface);

            if (classNode == null) {
                continue;
            }

            if (Util.isFlag(classNode.access, ACC_LIB) && AsmUtil.containsSuperMethod(classNode, method.name)) {
                return true;
            }

            if (isInterfaceMethod(classNode, method)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isSuperMethod(ClassNode classNode, MethodNode method) {
        while (true) {
            classNode = obfuscator.nodeByName.get(classNode.superName);

            if (classNode == null) {
                return false;
            }

            if (Util.isFlag(classNode.access, ACC_LIB) && AsmUtil.containsSuperMethod(classNode, method.name)) {
                return true;
            }

            if (isInterfaceMethod(classNode, method)) {
                return true;
            }
        }
    }

    @Override
    public TransformerType getType() {
        return TransformerType.METHOD_RENAME;
    }

    public static class SuperRenamer extends Transformer {

        @Override
        public TransformerType getType() {
            return TransformerType.METHOD_RENAME;
        }

        @Override
        public void transform(ClassNode node) {
            addSuperMethods(node, node);
            addInterfacesMethods(node, node);
        }

        protected void addInterfacesMethods(ClassNode original, ClassNode findIn) {
            if (!findIn.interfaces.isEmpty()) {
                for (String anInterface : findIn.interfaces) {
                    findIn = obfuscator.nodeByName.get(anInterface);

                    if (findIn == null) {
                        continue;
                    }

                    if (!findIn.methods.isEmpty() && Util.noFlag(findIn.access, ACC_LIB)) {
                        for (MethodNode method : findIn.methods) {
                            if (Util.noFlag(method.access, ACC_SKIPPED)) {
                                obfuscator.mapper.methodHasher.putReplaceOwner(findIn.name, original.name, method.name, method.desc);
                            }
                        }
                    }

                    addInterfacesMethods(original, findIn);
                }
            }
        }

        protected void addSuperMethods(ClassNode original, ClassNode findIn) {
            while (true) {
                findIn = obfuscator.nodeByName.get(findIn.superName);

                if (findIn == null) {
                    return;
                }

                if (!findIn.methods.isEmpty() && Util.noFlag(findIn.access, ACC_LIB)) {
                    for (MethodNode method : findIn.methods) {
                        if (Util.noFlag(method.access, ACC_SKIPPED)) {
                            obfuscator.mapper.methodHasher.putReplaceOwner(findIn.name, original.name, method.name, method.desc);
                        }
                    }
                }
                addInterfacesMethods(original, findIn);
            }
        }
    }
}
