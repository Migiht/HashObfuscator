package by.m1ght.transformer.other;

import by.m1ght.Obfuscator;
import by.m1ght.util.AsmUtil;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.util.UniqueStringGenerator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

// Useless
public class StringTransformer extends Transformer {

    private static final int PARTITION_BITS = 8;
    private static final int PARTITION_SIZE = 1 << PARTITION_BITS;
    private static final int PARTITION_MASK = PARTITION_SIZE - 1;

    private final List<String> strings = new ObjectArrayList<>();

    private String className;
    private String fieldName;

    @Override
    public void init(Obfuscator obf) {
        super.init(obf);
        className = config.params.getOrDefault("CLASS_NAME", UniqueStringGenerator.get(obfuscator.classNameGeneratorID.getAndIncrement()));
        fieldName = config.params.getOrDefault("FIELD_NAME", "a");
    }

    @Override
    public void transform(ClassNode classNode) {
       /* for (MethodNode method : classNode.methods) {
            for (Iterator<AbstractInsnNode> iter = method.instructions.iterator(); iter.hasNext(); ) {
                AbstractInsnNode insn = iter.next();
                if (insn.getOpcode() == Opcodes.LDC) {
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if (ldc.cst instanceof String) {
                        String string = (String) ldc.cst;
                        int id = strings.indexOf(string);
                        if (id == -1) {
                            id = strings.size();
                            strings.add(string);
                        }
                        Random random = ThreadLocalRandom.current();
                        int index = id & PARTITION_MASK;
                        int classId = id >> PARTITION_BITS;
                        int mask = (short) random.nextInt();
                        int a = (short) random.nextInt() & mask | index;
                        int b = (short) random.nextInt() & ~mask | index;
                        method.instructions.insertBefore(insn, new FieldInsnNode(Opcodes.GETSTATIC, (className + classId).intern(), fieldName, "[Ljava/lang/String;"));
                        method.instructions.insertBefore(insn, AsmUtil.pushInt(a));
                        method.instructions.insertBefore(insn, AsmUtil.pushInt(b));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.IAND));
                        method.instructions.insertBefore(insn, new InsnNode(Opcodes.AALOAD));
                        iter.remove();
                    }
                }
            }
        }

        // ON FINISH
        {
            for (int classId = 0; classId <= strings.size() >> PARTITION_BITS; classId++) {
                ClassNode classNode = AsmUtil.createNode(className + classId);
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, fieldName, "[Ljava/lang/String;", null, null));
                MethodNode clinit = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                classNode.methods.add(clinit);
                int start = classId << PARTITION_BITS;
                int end = Math.min(start + PARTITION_SIZE, strings.size());
                clinit.instructions.add(AsmUtil.pushInt(end - start));
                clinit.instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"));
                for (int id = start; id < end; id++) {
                    clinit.instructions.add(new InsnNode(Opcodes.DUP));
                    clinit.instructions.add(AsmUtil.pushInt(id & PARTITION_MASK));
                    clinit.instructions.add(new LdcInsnNode(strings.get(id)));
                    clinit.instructions.add(new InsnNode(Opcodes.AASTORE));
                }
                clinit.instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, classNode.name, fieldName, "[Ljava/lang/String;"));
                clinit.instructions.add(new InsnNode(Opcodes.RETURN));
                generated.add(classNode);
            }
        }*/
    }

}
