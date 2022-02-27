import by.m1ght.util.AsmUtil;
import by.m1ght.util.LogUtil;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.zip.ZipEntry;

public class Replacer {
    private final Path file;
    private final Set<String> stringSet = new HashSet<>();
    private final List<ClassNode> nodes = new ArrayList<>();
    private final List<ProguardConfigPart> proguardConfig = new ArrayList<>();
    private final Path proguardConfigFile;

    public Replacer(String file, String proguardConfigFile) {
        this.file = Paths.get(file);
        this.proguardConfigFile = Paths.get(proguardConfigFile);
    }

    public void loadInput() throws Throwable {
        try (ZipArchiveInputStream stream = new ZipArchiveInputStream(Files.newInputStream(file))) {
            ReadableByteChannel channel = Channels.newChannel(stream);

            ZipEntry entry;
            while ((entry = stream.getNextZipEntry()) != null) {

                if (!entry.isDirectory()) {
                    String name = entry.getName();

                    try {
                        ByteBuffer buffer = ByteBuffer.wrap(new byte[4096]);

                        for (;;) {
                            if (channel.read(buffer) <= 0 && buffer.hasRemaining()) {
                                buffer.limit(buffer.position());
                                buffer.flip();
                                break;
                            }
                            if (!buffer.hasRemaining()) {
                                buffer = ByteBuffer.wrap(Arrays.copyOf(buffer.array(), buffer.capacity() * 2));
                                buffer.position(buffer.capacity() / 2);
                            }

                            if (name.endsWith(".class") && !entry.getName().contains("META-INF")) {
                                ClassReader reader = new ClassReader(buffer.array());
                                ClassNode node = new ClassNode();
                                reader.accept(node, AsmUtil.getInputReaderFlags());

                                nodes.add(node);
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        LogUtil.warning("Класс %s не может быть загружен из исходного файла", name);
                    }
                }
            }
            LogUtil.info("Загружено %s классов из исходного файла", nodes.size());
        }
    }

    public void findStrings() throws Throwable {
        for (ClassNode node : nodes) {
            for (MethodNode method : node.methods) {
                for (AbstractInsnNode instruction : method.instructions) {
                    if (instruction.getType() == AbstractInsnNode.LDC_INSN) {
                        LdcInsnNode string = (LdcInsnNode) instruction;
                        if (string.cst instanceof String) {
                            stringSet.add((String) string.cst);
                        }
                    }
                }
            }
        }
    }

    public void replaceNames() {
        for (ClassNode node : nodes) {
            for (MethodNode method : node.methods) {

            }

            for (FieldNode field : node.fields) {

            }
        }
    }

    public void saveData() {
        try {
            Files.createDirectories(proguardConfigFile.getParent());
            List<String> data = new ArrayList<>();

            String tabSymbol = "    "; // 4 spaces

            for (ProguardConfigPart part : proguardConfig) {
                data.add(part.owner + " -> " + part.newOwner + ":");

                for (Map.Entry<NodeData, String> entry : part.methodList.entrySet()) {

                    StringBuilder args = new StringBuilder();
                    Type[] argumentTypes = Type.getArgumentTypes(entry.getKey().desc);

                    for (int i = 0; i < argumentTypes.length; i++) {
                        Type argumentType = argumentTypes[i];
                        args.append(argumentType.getClassName());
                        if (i + 1 < argumentTypes.length) {
                            args.append(',');
                        }
                    }

                    data.add(tabSymbol
                            + Type.getReturnType(entry.getKey().desc).getClassName()
                            + " " + entry.getKey().name
                            + args
                            + " -> "
                            + entry.getValue()
                    );
                }

                for (Map.Entry<NodeData, String> entry : part.fieldList.entrySet()) {
                    data.add(tabSymbol + entry.getKey());
                }

            }

            Files.write(proguardConfigFile, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ProguardConfigPart {
        public final String owner;
        public final String newOwner;
        public final Map<NodeData, String> methodList = new HashMap<>();
        public final Map<NodeData, String> fieldList = new HashMap<>();

        public ProguardConfigPart(String owner, String newOwner) {
            this.owner = owner;
            this.newOwner = newOwner;
        }
    }

    private static class NodeData {
        public final String name;
        public final String desc;

        public NodeData(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }
}
