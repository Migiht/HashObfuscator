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
    public static final String PROGUARD_TAB = "    ";
    public static final String PROGUARD_SPLIT = " -> ";

    private final Path srcPath;

    private final List<InputJar> sources = new ArrayList<>();
    private final Map<String, String> ldcMap = new HashMap<>();

    private final List<ProguardConfigSegment> proguardConfig = new ArrayList<>();
    private final Path proguardConfigPath;
    public static int nextID = Short.MAX_VALUE;

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

                                for (; ; ) {
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
                    AsmUtil.processLDCString(method, (ldcNode, s) -> ldcMap.put(AsmUtil.toAsmName(s), null));
                }
            }
        }
    }

    public void replaceNames() {
        for (InputJar nextJar : sources) {
            for (ClassNode node : nextJar.nodes) {
                ProguardConfigSegment segment = null;

                if (ldcMap.containsKey(node.name) || ldcMap.containsKey(AsmUtil.toPointName(node.name))) {
                    ProguardConfigSegment configPart = new ProguardConfigSegment(node.name, UniqueStringGenerator.get(nextID++));
                    ldcMap.put(configPart.owner, configPart.newOwner);
                    ldcMap.put(AsmUtil.toPointName(configPart.owner), configPart.newOwner);
                }

                for (MethodNode method : node.methods) {
                    if (ldcMap.containsKey(method.name)) {
                        if (segment == null) {
                            segment = new ProguardConfigSegment(node.name, UniqueStringGenerator.get(nextID++));
                            ldcMap.put(segment.owner, segment.newOwner);
                        }
                        ldcMap.put(method.name, UniqueStringGenerator.get(nextID++));
                        segment.methods.add(new NodeNameDesc(method, method.name));
                    }
                }

                for (FieldNode field : node.fields) {
                    if (ldcMap.containsKey(field.name)) {
                        if (segment == null) {
                            segment = new ProguardConfigSegment(node.name, UniqueStringGenerator.get(nextID++));
                            ldcMap.put(segment.owner, segment.newOwner);
                        }

                        ldcMap.put(field.name, UniqueStringGenerator.get(nextID++));
                        segment.fields.add(new NodeNameDesc(field, ldcMap.get(field.name)));
                    }
                }

                if (segment != null) {
                    proguardConfig.add(segment);
                }
            }
        }
    }

    public void saveData() {
        System.out.println(ldcMap);
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

                    ZipEntry zipEntry = IOUtil.newZipEntry(node.name + ".class");

                    output.putNextEntry(zipEntry);
                    output.write(writer.toByteArray());
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
            for (ProguardConfigSegment cfg : proguardConfig) {
                data.addAll(cfg.serialize());
            }
            Files.write(proguardConfigPath, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ProguardConfigSegment {
        public final String owner;
        public final String newOwner;
        public final List<NodeNameDesc> methods = new LinkedList<>();
        public final List<NodeNameDesc> fields = new LinkedList<>();

        public ProguardConfigSegment(String owner, String newOwner) {
            this.owner = AsmUtil.toPointName(owner);
            this.newOwner = AsmUtil.toPointName(newOwner);
        }

        public static List<ProguardConfigSegment> deserialize(List<String> input) {
            List<ProguardConfigSegment> segments = new ArrayList<>();
            for (String nextStr : input) {
                // if part of a class
                if (nextStr.startsWith(" ")) {
                    ProguardConfigSegment last = segments.get(segments.size() - 1);

                    String[] oldNewName = nextStr.split(PROGUARD_SPLIT);
                    String[] descNameArgs = oldNewName[0].split(" ");
                    NodeNameDesc node = new NodeNameDesc(oldNewName[0], oldNewName[1]);

                    // is a method or else field
                    if (nextStr.contains("(")) {
                        node.desc = descNameArgs[0];
                        last.methods.add(node);
                   } else {
                        node.returnType = descNameArgs[0];
                        node.desc = last.splitName(descNameArgs[1]);
                        last.fields.add(node);
                   }
                } else { // this is a class
                    String[] parts = nextStr.split(PROGUARD_SPLIT);
                    if (parts.length != 2) throw new RuntimeException("Fucking broken cfg");
                    segments.add(new ProguardConfigSegment(parts[0], parts[1]));
                }
            }
            return segments;
        }

        public String parseMethodArg(String desc) {
            StringBuilder args = new StringBuilder("(");
            Type[] argumentTypes = Type.getArgumentTypes(desc);

            for (int i = 0; i < argumentTypes.length; i++) {
                Type argumentType = argumentTypes[i];
                args.append(argumentType.getClassName());
                if (i + 1 < argumentTypes.length) {
                    args.append(',');
                }
            }
            args.append(")");
            return args.toString();
        }

        public String splitName(String nameDesc) {
            return nameDesc.substring(0, nameDesc.indexOf('('));
        }

        public void convertDesc() {
            for (NodeNameDesc method : methods) {
                method.returnType = Type.getReturnType(method.desc).getClassName();
                method.desc = parseMethodArg(method.desc);
            }

            for (NodeNameDesc field : fields) {
                field.desc = Type.getReturnType(field.desc).getClassName();
            }
        }

        public List<String> serialize() {
            List<String> data = new ArrayList<>();
            data.add(this.owner
                    + PROGUARD_SPLIT // ->
                    + this.newOwner + ":");

            for (NodeNameDesc entry : this.methods) {

                data.add(PROGUARD_TAB
                        + entry.returnType
                        + " "
                        + entry.name
                        + entry.desc
                        + PROGUARD_SPLIT // ->
                        + entry.newName
                );
            }

            for (NodeNameDesc entry : this.fields) {
                data.add(PROGUARD_TAB +
                        Type.getType(entry.desc).getClassName()
                        + " "
                        + entry.name
                        + PROGUARD_SPLIT // ->
                        + entry.newName
                );
            }
            return data;
        }
    }

    private static class NodeNameDesc {
        public final String name;
        public String desc;
        public final String newName;

        // For methods only
        public String returnType;

        public NodeNameDesc(MethodNode node, String newName) {
            this.name = node.name;
            this.desc = AsmUtil.toPointName(node.desc);
            this.newName = newName;
        }

        public NodeNameDesc(FieldNode node, String newName) {
            this.name = node.name;
            this.desc = AsmUtil.toPointName(node.desc);
            this.newName = newName;
        }

        public NodeNameDesc(String name, String newName) {
            this.name = name;
            this.newName = newName;
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
