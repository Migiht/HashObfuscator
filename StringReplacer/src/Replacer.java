import by.m1ght.util.AsmUtil;
import by.m1ght.util.IOUtil;
import by.m1ght.util.LogUtil;
import by.m1ght.util.UniqueStringGenerator;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Replacer {
    private final Path srcPath;

    private final List<InputJar> sources = new ArrayList<>();
    private final Map<String, String> ldcMap = new HashMap<>();
    private final List<ProguardConfigPart> proguardConfig = new ArrayList<>();
    private final Path proguardConfigPath;
    private int id = Short.MAX_VALUE;

    public Replacer(String srcPath, String proguardCfgPath) {
        this.srcPath = Paths.get(srcPath);
        this.proguardConfigPath = Paths.get(proguardCfgPath);
    }

    public void loadInput() throws Throwable {
        Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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
                                }

                                InputJar jar = new InputJar(file);
                                if (name.endsWith(".class")) {
                                    ClassReader reader = new ClassReader(buffer.array());
                                    ClassNode node = new ClassNode();
                                    reader.accept(node, AsmUtil.getInputReaderFlags());

                                    jar.nodes.add(node);
                                } else {
                                    jar.data.add(buffer);
                                }
                                sources.add(jar);

                            } catch (Throwable e) {
                                e.printStackTrace();
                                LogUtil.warning("Класс %s не может быть загружен из исходного файла", name);
                            }
                        }
                    }
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    public void findStrings() {
        for (InputJar nextJar : sources) {
            for (ClassNode node : nextJar.nodes) {
                for (MethodNode method : node.methods) {
                    for (AbstractInsnNode instruction : method.instructions) {
                        if (instruction.getType() == AbstractInsnNode.LDC_INSN) {
                            LdcInsnNode string = (LdcInsnNode) instruction;
                            if (string.cst instanceof String) {
                                ldcMap.put(AsmUtil.toAsmName((String) string.cst), null);
                            }
                        }
                    }
                }
            }
        }
    }

    protected ProguardConfigPart createPart(String findName, boolean bypass) {
        if (bypass || ldcMap.containsKey(findName)) {
            ProguardConfigPart configPart = new ProguardConfigPart(findName);
            String nextGenerated = UniqueStringGenerator.get(id++);

            configPart.newOwner = nextGenerated;
            ldcMap.put(findName, nextGenerated);
            return configPart;
        }
        return null;
    }

    public void replaceNames() {
        String nextGenerated;
        for (InputJar nextJar : sources) {
            for (ClassNode node : nextJar.nodes) {
                ProguardConfigPart configPart = null;

                if (ldcMap.containsKey(node.name)) {
                    configPart = createPart(node.name, false);
                }

                if (ldcMap.containsKey(AsmUtil.toPointName(node.name))) {
                    configPart = createPart(AsmUtil.toPointName(node.name), false);
                }

                for (MethodNode method : node.methods) {
                    if (ldcMap.containsKey(method.name)) {
                        configPart = createPart(node.name, true);

                        nextGenerated = UniqueStringGenerator.get(id++);
                        configPart.methods.put(new NodeData(method), nextGenerated);
                        ldcMap.put(method.name, nextGenerated);
                    }
                }

                for (FieldNode field : node.fields) {
                    if (ldcMap.containsKey(field.name)) {
                        configPart = createPart(node.name, true);

                        nextGenerated = UniqueStringGenerator.get(id++);
                        configPart.fields.put(new NodeData(field), nextGenerated);
                        ldcMap.put(field.name, nextGenerated);
                    }
                }

                if (configPart != null) {
                    proguardConfig.add(configPart);
                }
            }
        }
    }

    public void saveData() {
        try {
            Files.createDirectories(proguardConfigPath.getParent());

            for (InputJar source : sources) {
                ZipOutputStream output = IOUtil.newZipOutput(source.path);

                for (ClassNode node : source.nodes) {
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                    for (MethodNode method : node.methods) {
                        AsmUtil.processLDCString(method, (ldcNode, s) -> ldcNode.cst = ldcMap.getOrDefault((String) ldcNode.cst, (String) ldcNode.cst));
                    }
                    byte[] array = writer.toByteArray();

                    ZipEntry zipEntry = IOUtil.newZipEntry(node.name + ".class");
                    output.putNextEntry(zipEntry);
                    output.write(array);
                }

                for (Map.Entry<String, ByteBuffer> entry : source.data.entrySet()) {
                    ZipEntry zipEntry = IOUtil.newZipEntry(entry.getKey());

                    output.putNextEntry(zipEntry);
                    output.write(entry.getValue().array());
                }

            }

            List<String> data = new ArrayList<>();

            String tabSymbol = "    "; // 4 spaces

            for (ProguardConfigPart part : proguardConfig) {
                data.add(part.owner + " -> " + part.newOwner + ":");

                for (Map.Entry<NodeData, String> entry : part.methods.entrySet()) {
                    StringBuilder args = new StringBuilder("(");
                    Type[] argumentTypes = Type.getArgumentTypes(entry.getKey().desc);

                    for (int i = 0; i < argumentTypes.length; i++) {
                        Type argumentType = argumentTypes[i];
                        args.append(argumentType.getClassName());
                        if (i + 1 < argumentTypes.length) {
                            args.append(',');
                        }
                    }
                    args.append(")");

                    data.add(tabSymbol
                            + Type.getReturnType(entry.getKey().desc).getClassName()
                            + " " + entry.getKey().name
                            + args
                            + " -> "
                            + entry.getValue()
                    );
                }

                for (Map.Entry<NodeData, String> entry : part.fields.entrySet()) {
                    data.add(tabSymbol +
                            Type.getType(entry.getKey().desc).getClassName()
                            + " "
                            + entry.getKey().name
                            + " -> "
                            + entry.getValue()
                    );
                }

            }

            Files.write(proguardConfigPath, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ProguardConfigPart {
        public final String owner;
        public String newOwner;
        public final Map<NodeData, String> methods = new HashMap<>();
        public final Map<NodeData, String> fields = new HashMap<>();

        public ProguardConfigPart(String owner) {
            this.owner = AsmUtil.toPointName(owner);
        }
    }

    private static class NodeData {
        public final String name;
        public final String desc;

        public NodeData(MethodNode node) {
            this.name = node.name;
            this.desc = AsmUtil.toPointName(node.desc);
        }

        public NodeData(FieldNode node) {
            this.name = node.name;
            this.desc = AsmUtil.toPointName(node.desc);
        }

        public NodeData(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }
    }

    private static class InputJar {
        public final Path path;
        public final List<ClassNode> nodes = new ArrayList<>();
        public final Map<String, ByteBuffer> data = new HashMap<>();

        public InputJar(Path path) {
            this.path = path;
        }
    }
}
