package by.m1ght.io;

import by.m1ght.transformer.Transformer;
import by.m1ght.util.AsmUtil;
import by.m1ght.util.LogUtil;
import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class LibFileVisitor extends SimpleFileVisitor<Path> {
    private final Map<String, ClassNode> nodes = new Object2ObjectArrayMap<>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile() && file.toString().endsWith(".jar")) {

            long startTime = System.nanoTime();

            try (ZipArchiveInputStream stream = new ZipArchiveInputStream(Files.newInputStream(file))) {
                ReadableByteChannel channel = Channels.newChannel(stream);
                ZipEntry entry;

                while ((entry = stream.getNextZipEntry()) != null) {

                    if (!entry.isDirectory()) {
                        String name = entry.getName();

                        if (name.endsWith(".class")) {
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

                                ClassReader reader = new ClassReader(buffer.array());
                                ClassNode classNode = new ClassNode();
                                reader.accept(classNode, AsmUtil.getLibReaderFlags());
                                classNode.access = Util.setFlag(classNode.access, Transformer.ACC_LIB);
                                nodes.put(classNode.name, classNode);
                            } catch (IllegalArgumentException | IOException e) {
                                LogUtil.error("Class %s load ERROR. Skip class in %s", name, file.toAbsolutePath());
                            }
                        }
                    }
                }
            }

            LogUtil.info("Load lib : " + file + " at " + ((System.nanoTime() - startTime) / 1_000_000) + " ms.");
        }
        return super.visitFile(file, attrs);
    }

    public Map<String, ClassNode> getNodes() {
        return nodes;
    }
}
