package by.m1ght.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.*;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AsmUtil {
    public static boolean isPushInt(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        return (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5)
                || op == Opcodes.BIPUSH
                || op == Opcodes.SIPUSH
                || (op == Opcodes.LDC && ((LdcInsnNode) insn).cst instanceof Integer);
    }

    public static int getPushedInt(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op >= Opcodes.ICONST_M1 && op <= Opcodes.ICONST_5) {
            return op - Opcodes.ICONST_0;
        }
        if (op == Opcodes.BIPUSH || op == Opcodes.SIPUSH) {
            return ((IntInsnNode) insn).operand;
        }
        if (op == Opcodes.LDC) {
            Object cst = ((LdcInsnNode) insn).cst;
            if (cst instanceof Integer) {
                return (int) cst;
            }
        }
        throw new IllegalArgumentException("insn is not a push int instruction");
    }

    public static AbstractInsnNode pushInt(int value) {
        if (value >= -1 && value <= 5) {
            return new InsnNode(Opcodes.ICONST_0 + value);
        }
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            return new IntInsnNode(Opcodes.BIPUSH, value);
        }
        if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            return new IntInsnNode(Opcodes.SIPUSH, value);
        }
        return new LdcInsnNode(value);
    }

    public static boolean isPushLong(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        return op == Opcodes.LCONST_0
                || op == Opcodes.LCONST_1
                || (op == Opcodes.LDC && ((LdcInsnNode) insn).cst instanceof Long);
    }

    public static long getPushedLong(AbstractInsnNode insn) {
        int op = insn.getOpcode();
        if (op == Opcodes.LCONST_0) {
            return 0;
        }
        if (op == Opcodes.LCONST_1) {
            return 1;
        }
        if (op == Opcodes.LDC) {
            Object cst = ((LdcInsnNode) insn).cst;
            if (cst instanceof Long) {
                return (long) cst;
            }
        }
        throw new IllegalArgumentException("insn is not a push long instruction");
    }

    public static AbstractInsnNode pushLong(long value) {
        if (value == 0) {
            return new InsnNode(Opcodes.LCONST_0);
        }
        if (value == 1) {
            return new InsnNode(Opcodes.LCONST_1);
        }
        return new LdcInsnNode(value);
    }

    public static int codeSize(MethodNode methodNode) {
        CodeSizeEvaluator evaluator = new CodeSizeEvaluator(null);
        methodNode.accept(evaluator);
        return evaluator.getMaxSize();
    }

    public static String getReturnType(String desc) {
        int index = desc.indexOf(')');
        int length = desc.length();

        if (index + 2 != desc.length()) {
            index = index + 1;
            length = length - 1;
        }
        return desc.substring(index + 1, length);
    }

    public static boolean containsSuperMethod(ClassNode classNode, String name) {
        if (!classNode.methods.isEmpty()) {
            for (MethodNode method : classNode.methods) {
                if (Util.noFlag(method.access, Opcodes.ACC_PRIVATE)
                        && Util.noFlag(method.access, Opcodes.ACC_STATIC)
                        && method.name.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static MethodNode getMethodNode(ClassNode classNode, String name) {
        if (!classNode.methods.isEmpty()) {
            for (MethodNode method : classNode.methods) {
                if (Util.noFlag(method.access, Opcodes.ACC_PRIVATE)
                        && method.name.equals(name)) {
                    return method;
                }
            }
        }
        return null;
    }

    public static void processLDCString(MethodNode method, BiConsumer<LdcInsnNode, String> consumer) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getType() == AbstractInsnNode.LDC_INSN) {
                if (((LdcInsnNode) instruction).cst instanceof String) {
                    consumer.accept((LdcInsnNode) instruction, (String) ((LdcInsnNode) instruction).cst);
                }
            }
        }
    }

    public static ClassNode createNode(String name) {
        ClassNode node = new ClassNode();
        node.version = Opcodes.V1_8;
        node.access = Opcodes.ACC_PUBLIC;
        node.name = name;
        node.superName = "java/lang/Object";

      /*node.visit(Opcodes.V1_5, ACC_SUPER | ACC_PUBLIC, name, null, "java/lang/Object", null);
        MethodNode init = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        init.instructions.add(new InsnNode(Opcodes.RETURN));
        init.visitMaxs(1, 1);
        node.methods.add(init);*/
        return node;
    }

    public static String getAsmName(String name) {
        return name.replace('.', '/');
    }

    public static int getLibReaderFlags() {
        return ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE;
    }

    public static int getInputReaderFlags() {
        return ClassReader.SKIP_DEBUG;
    }

    public static int changeToPublic(int access) {
        return access & (~Opcodes.ACC_PROTECTED & ~Opcodes.ACC_PRIVATE) | Opcodes.ACC_PUBLIC;
    }

    public static int changeToNotFinal(int access) {
        return access & ~Opcodes.ACC_FINAL;
    }

    public static int parseKeyWordToAccess(String keyword) {
        // From  ASM  Opcodes
        switch (keyword) {
            case "public"       :        return 1;
            case "private"      :        return 2;
            case "protected"    :        return 4;
            case "static"       :        return 8;
            case "final"        :        return 16;
            case "super"        :        return 32;
            case "synchronized" :        return 32;
            case "open"         :        return 32;
            case "transitive"   :        return 32;
            case "volatile"     :        return 64;
            case "bridge"       :        return 64;
            case "static_phase" :        return 64;
            case "varargs"      :        return 128;
            case "transient"    :        return 128;
            case "native"       :        return 256;
            case "interface"    :        return 512;
            case "abstract"     :        return 1024;
            case "strict"       :        return 2048;
            case "synthetic"    :        return 4096;
            case "annotation"   :        return 8192;
            case "enum"         :        return 16384;
            case "mandated"     :        return 32768;
            case "module"       :        return 32768;
            case "record"       :        return 65536;
            case "deprecated"   :        return 131072;
            default: return 0;
        }
    }
}