package by.m1ght;

import by.m1ght.asm.common.ObfClassRemapper;
import by.m1ght.asm.common.ObfRemapper;
import by.m1ght.config.Config;
import by.m1ght.config.TransformerConfig;
import by.m1ght.io.LibFileVisitor;
import by.m1ght.transformer.Transformer;
import by.m1ght.transformer.TransformerType;
import by.m1ght.transformer.classnode.ClassPublicAccesser;
import by.m1ght.transformer.classnode.ClassRenamer;
import by.m1ght.transformer.classnode.InnerClassRemover;
import by.m1ght.transformer.field.FieldPublicAccesser;
import by.m1ght.transformer.field.FieldRenamer;
import by.m1ght.transformer.field.FieldShuffler;
import by.m1ght.transformer.local.LocalRenamer;
import by.m1ght.transformer.method.*;
import by.m1ght.transformer.other.DebugRemover;
import by.m1ght.transformer.other.LDCCache;
import by.m1ght.transformer.other.StringRandomizer;
import by.m1ght.transformer.other.StringTransformer;
import by.m1ght.transformer.pack.Repackager;
import by.m1ght.util.*;
import it.unimi.dsi.fastutil.objects.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Obfuscator implements Runnable {

    private final Config config;

    private final Map<String, ByteBuffer> resources = new Object2ObjectArrayMap<>(Byte.MAX_VALUE);

    public final Map<String, ClassNode> nodeByName = new Object2ObjectOpenHashMap<>();

    public final Object2ObjectMap<Object, BitSet> excludeByNode = new Object2ObjectOpenHashMap<>();

    private final ObjectList<ClassNode> nodeList = new ObjectArrayList<>();

    private final ObjectList<Transformer> transformers = new ObjectArrayList<>();

    public final ObfRemapper mapper = new ObfRemapper(this);

    public Obfuscator(Config cfg) {
        config = cfg;
        LogUtil.setDebugEnabled(cfg.debug);
        UniqueStringGenerator.setCaching(cfg.caching);
    }

    private void initTransformers() {
        int transformerID = 0;
        TransformerConfig global = config.transformerConfigMap.get(TransformerType.GLOBAL);
        for (Iterator<Transformer> iterator = transformers.iterator(); iterator.hasNext();) {
            Transformer worker = iterator.next();

            TransformerConfig workerCfg = config.transformerConfigMap.get(worker.getType());
            if (workerCfg != null && workerCfg.enabled && global.enabled) {
                worker.init(this);
                worker.setId(transformerID++);
            } else {
                iterator.remove();
                continue;
            }

            worker.applyConfig(config.transformerConfigMap.get(worker.getType()));
            worker.applyConfig(global);
        }
    }

    private void loadTransformers() {
        transformers.add(new LDCCache());
        transformers.add(new DebugRemover());
        transformers.add(new Repackager());
        transformers.add(new StringRandomizer());
        transformers.add(new StringTransformer());
        transformers.add(new InnerClassRemover());

        transformers.add(new ClassPublicAccesser());
        transformers.add(new FieldPublicAccesser());
        transformers.add(new MethodPublicAccesser());

        transformers.add(new StaticMethodMover());

        transformers.add(new MethodShuffler());
        transformers.add(new FieldShuffler());

        transformers.add(new ClassRenamer());
        transformers.add(new LocalRenamer());
        transformers.add(new MethodRenamer());
        transformers.add(new MethodRenamer.SuperRenamer());

        transformers.add(new INDYRenamer());
        transformers.add(new FieldRenamer());
        transformers.add(new FieldRenamer.SuperRenamer());
    }

    private void computeExcludes() {
        config.computeExcludes();
        for (Transformer transformer : transformers) {
            for (ClassNode classNode : nodeList) {
                transformer.computeClassExcludes(classNode);
            }
        }
    }

    private void loadInputFile() throws IOException {
        if (config.inputPath == null || Files.isDirectory(Paths.get(config.inputPath)) || !Files.exists(Paths.get(config.inputPath))) {
            throw new RuntimeException("Input null");
        }

        try (ZipArchiveInputStream stream = new ZipArchiveInputStream(Files.newInputStream(Paths.get(config.inputPath)))) {
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


                        if (name.endsWith(".class") && !entry.getName().contains("META-INF")) {
                            ClassReader reader = new ClassReader(buffer.array());
                            ClassNode node = new ClassNode();
                            reader.accept(node, AsmUtil.getInputReaderFlags());

                            nodeByName.put(node.name, node);
                            nodeList.add(node);
                        } else {
                            resources.put(name, buffer);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        LogUtil.warning("Класс %s не может быть загружен из исходного файла", name);
                    }
                }
            }
            LogUtil.info("Загружено %s классов из исходного файла", nodeList.size());
        }

    }


    private void saveOutputFile() throws IOException {
        try (ZipOutputStream output = IOUtil.newZipOutput(config.outputPath)) {

            if (config.crashCRC) {
                Util.crashCRC(output);
            }

            Map<String, String> classNames = mapper.getClassNames();

            resources.entrySet().parallelStream().forEach(resource -> {
                ZipEntry entry = IOUtil.newZipEntry(resource.getKey());
                ByteBuffer value = resource.getValue();

                    try {
                        if (value.position() < 10_000 && entry.getName().contains("META-INF")) {
                            String line = new String(value.array(), 0, value.position());

                            for (Map.Entry<String, String> stringEntry : classNames.entrySet()) {
                                line = line.replaceAll(stringEntry.getKey(), stringEntry.getValue());
                                line = line.replaceAll(stringEntry.getKey().replace('/', '.'), stringEntry.getValue().replace('/', '.'));
                            }

                            ByteBuffer replaced = ByteBuffer.wrap(line.getBytes(StandardCharsets.UTF_8));
                            replaced.position(line.length());
                            value = replaced;
                        }

                        synchronized (output) {
                            output.putNextEntry(entry);
                            output.write(value.array(), 0, value.position());
                            output.closeEntry();
                        }

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
            });

            ThreadLocal<ObfClassRemapper> obfVisitor = ThreadLocal.withInitial(() -> new ObfClassRemapper(null, mapper));

            nodeList.parallelStream().forEach((node) -> {
                try {
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    node.accept(obfVisitor.get().setVisitor(writer));
                    //node.accept(writer);
                    ZipEntry entry = IOUtil.newZipEntry(classNames.getOrDefault(node.name, node.name) + ".class");
                    byte[] array = writer.toByteArray();

                    synchronized (output) {
                        output.putNextEntry(entry);
                        output.write(array);
                        output.closeEntry();
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    private List<CompletableFuture<Void>> loadLibs() {
        List<CompletableFuture<Void>> futures = new ObjectArrayList<>();

        config.libs.addAll(Util.getDefaultJreLibs());

        for (String lib : config.libs) {
            if (Files.exists(Paths.get(lib))) {
                CompletableFuture<Void> libVisitor = CompletableFuture.supplyAsync(() -> {
                    LibFileVisitor visitor = new LibFileVisitor();
                    try {
                        Files.walkFileTree(Paths.get(lib), visitor);
                    } catch (IOException e) {
                        LogUtil.error(e.getMessage());
                        e.printStackTrace();
                    }
                    return visitor;
                }).thenAccept((result) -> nodeByName.putAll(result.getNodes()));

                futures.add(libVisitor);
            }
        }
        return futures;
    }

    @Override
    public void run() {
        //TODO CW -> COMPUTE_MAX fix
        long startTime = System.nanoTime();

        LogUtil.info("Loading libs...");

        List<CompletableFuture<Void>> futures = loadLibs();

        LogUtil.info("Loading input... " + config.inputPath);
        try {
            loadInputFile();
        } catch (IOException e) {
            LogUtil.error("Cannot load input");
            e.printStackTrace();
            return;
        }
        Thread.yield();

        LogUtil.info("Shuffle");
        Collections.shuffle(nodeList, ThreadLocalRandom.current());

        for (CompletableFuture<Void> future : futures) {
            try {
                future.join();
            } catch (Throwable e) {
                LogUtil.error(e.getMessage());
                e.printStackTrace();
            }
        }

        loadTransformers();
        initTransformers();
        computeExcludes();

        LogUtil.info("Process input classes");

        for (Transformer transformer : transformers) {
            for (int i = 0; i < nodeList.size(); i++) {
                ClassNode node = nodeList.get(i);
                if (transformer.canTransformClass(node)) {
                    transformer.transform(node);
                }
            }
        }

        LogUtil.info("Save classes " + config.outputPath);

        try {
            saveOutputFile();
        } catch (IOException e) {
            LogUtil.error("Cannot save classes");
            e.printStackTrace();
        }

        LogUtil.info("Finished at " + (System.nanoTime() - startTime) / 1_000_000 + " ms.");
        LogUtil.info("Memory used (MBytes): " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
        LogUtil.info(mapper.toString());
    }
}