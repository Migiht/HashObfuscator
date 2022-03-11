package extension;

import by.m1ght.util.AsmUtil;
import by.m1ght.util.IOUtil;
import by.m1ght.util.LogUtil;
import by.m1ght.util.UniqueStringGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Replacer {
    public static final String OBF_SUFFIX = "-patched.jar";
    private final Path srcPath;

    private final List<InputJar> sources = new ArrayList<>();
    private final Map<String, String> ldcMap = new HashMap<>();

    private final List<ProguardConfigSegment> proguardConfig = new ArrayList<>();
    private final Path proguardConfigPath;

    private int id = Short.MAX_VALUE;

    public Replacer(String srcPath, String proguardCfgPath) {
        this.srcPath = Paths.get(srcPath).toAbsolutePath();
        this.proguardConfigPath = Paths.get(proguardCfgPath).toAbsolutePath();
    }

    public void loadInput() throws Throwable {
        Files.walkFileTree(srcPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(OBF_SUFFIX)) return super.visitFile(file, attrs);

                try (ZipInputStream stream = new ZipInputStream(Files.newInputStream(file))) {
                    ReadableByteChannel channel = Channels.newChannel(stream);

                    InputJar jar = new InputJar(file.toAbsolutePath());
                    sources.add(jar);
                    ZipEntry entry;

                    while ((entry = stream.getNextEntry()) != null) {

                        if (!entry.isDirectory()) {
                            String name = entry.getName();

                            try {
                                ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) entry.getSize()]);

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

                                if (name.endsWith(".class")) {
                                    ClassReader reader = new ClassReader(buffer.array(), 0, buffer.limit());
                                    ClassNode node = new ClassNode();
                                    reader.accept(node, AsmUtil.getInputReaderFlags());

                                    jar.nodes.add(node);
                                } else {
                                    jar.data.put(entry.getName(), buffer);
                                }

                            } catch (Throwable e) {
                                e.printStackTrace();
                                LogUtil.warning("Класс %s не может быть загружен из исходного файла", name);
                            }
                        }
                    }
                    channel.close();
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

    protected ProguardConfigSegment createPart(ProguardConfigSegment segment, String findName, boolean bypass) {
        if (segment == null && (bypass || ldcMap.containsKey(findName))) {
            ProguardConfigSegment configPart = new ProguardConfigSegment(findName);
            String nextGenerated = UniqueStringGenerator.get(id++);

            configPart.newOwner = nextGenerated;
            ldcMap.put(findName, nextGenerated);
            return configPart;
        }
        return segment;
    }

    public void replaceNames() {
        String nextGenerated;
        for (InputJar nextJar : sources) {
            for (ClassNode node : nextJar.nodes) {
                ProguardConfigSegment segment = null;

                segment = createPart(segment, node.name, false);
                segment = createPart(segment, AsmUtil.toPointName(node.name), false);

                for (MethodNode method : node.methods) {
                    if (ldcMap.containsKey(method.name)) {
                        segment = createPart(segment, node.name, true);

                        nextGenerated = UniqueStringGenerator.get(id++);
                        segment.methods.put(new NodeData(method), nextGenerated);
                        ldcMap.put(method.name, nextGenerated);
                    }
                }

                for (FieldNode field : node.fields) {
                    if (ldcMap.containsKey(field.name)) {
                        segment = createPart(segment, node.name, true);

                        nextGenerated = UniqueStringGenerator.get(id++);
                        segment.fields.put(new NodeData(field), nextGenerated);
                        ldcMap.put(field.name, nextGenerated);
                    }
                }

                if (segment != null) {
                    proguardConfig.add(segment);
                }
            }
        }
    }

    public void saveData() {
        System.out.println(ldcMap.toString());
        try {
            Files.createDirectories(proguardConfigPath.getParent());

            for (InputJar source : sources) {
                ZipOutputStream output = IOUtil.newZipOutput(source.path.resolveSibling(source.path.getFileName() + OBF_SUFFIX));

                for (ClassNode node : source.nodes) {
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                    for (MethodNode method : node.methods) {
                        AsmUtil.processLDCString(method, (ldcNode, s) -> {
                            String newName = ldcMap.get((String) ldcNode.cst);
                            if (newName != null) {
                                ldcNode.cst = newName;
                            }
                        });
                    }
                    node.accept(writer);
                    byte[] array = writer.toByteArray();

                    ZipEntry zipEntry = IOUtil.newZipEntry(node.name + ".class");
                    output.putNextEntry(zipEntry);
                    output.write(array);
                    output.closeEntry();
                }

                for (Map.Entry<String, ByteBuffer> entry : source.data.entrySet()) {
                    ZipEntry zipEntry = IOUtil.newZipEntry(entry.getKey());

                    output.putNextEntry(zipEntry);
                    output.write(entry.getValue().array());
                    output.closeEntry();
                }
                output.close();
            }

            List<String> data = new ArrayList<>();

            String tabSymbol = "    "; // 4 spaces

            for (ProguardConfigSegment part : proguardConfig) {
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

    private static class ProguardConfigSegment {
        public final String owner;
        public String newOwner;
        public final Map<NodeData, String> methods = new HashMap<>();
        public final Map<NodeData, String> fields = new HashMap<>();

        public ProguardConfigSegment(String owner) {
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
        public final Map<String, ByteBuffer> data = new Object2ObjectArrayMap<>();

        public InputJar(Path path) {
            this.path = path;
        }
    }
}
