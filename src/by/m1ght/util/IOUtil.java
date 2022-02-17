package by.m1ght.util;

import by.m1ght.Obfuscator;
import by.m1ght.gui.GuiObfuscator;
import com.google.gson.GsonBuilder;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class IOUtil {
    public static final int BUFFER_SIZE = 4096;

    public static final Charset UNICODE_CHARSET = StandardCharsets.UTF_8;

    private static final OpenOption[] READ_OPTIONS = {StandardOpenOption.READ};
    private static final OpenOption[] WRITE_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};
    private static final OpenOption[] APPEND_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND};
    private static final CopyOption[] COPY_OPTIONS = {StandardCopyOption.REPLACE_EXISTING};

    private static final Set<FileVisitOption> WALK_OPTIONS = Collections.singleton(FileVisitOption.FOLLOW_LINKS);

    public static void write(Path path, byte[] array) throws IOException {
        Files.write(path, array, WRITE_OPTIONS);
    }

    public static OutputStream newOutput(Path file) throws IOException {
        return Files.newOutputStream(file, WRITE_OPTIONS);
    }

    public static InputStream newInput(Path file) throws IOException {
        return Files.newInputStream(file, READ_OPTIONS);
    }

    public static ZipOutputStream newZipOutput(OutputStream output) {
        return new ZipOutputStream(output);
    }

    public static ZipOutputStream newZipOutput(Path file) throws IOException {
        return newZipOutput(newOutput(file));
    }

    public static ZipOutputStream newZipOutput(String name) throws IOException {
        return newZipOutput(Paths.get(name));
    }

    public static ZipInputStream newZipInput(InputStream input) {
        return new ZipInputStream(input);
    }

    public static ZipInputStream newZipInput(Path file) throws IOException {
        return newZipInput(newInput(file));
    }

    public static ZipInputStream newZipInput(String name) throws IOException {
        return newZipInput(Paths.get(name));
    }

    public static ZipFile newZipFile(String path) throws IOException {
        return new ZipFile(path);
    }

    public static ZipFile newZipFile(Path path) throws IOException {
        return newZipFile(path.toString());
    }

    public static BufferedWriter newWriter(Path file, boolean append) throws IOException {
        return Files.newBufferedWriter(file, UNICODE_CHARSET, append ? APPEND_OPTIONS : WRITE_OPTIONS);
    }
    public static BufferedReader newReader(InputStream input) {
        return newReader(input, UNICODE_CHARSET);
    }

    public static BufferedReader newReader(InputStream input, Charset charset) {
        return new BufferedReader(new InputStreamReader(input, charset));
    }

    public static BufferedReader newReader(Path file) throws IOException {
        return Files.newBufferedReader(file, UNICODE_CHARSET);
    }

    public static FileChooser newFileChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(getCodeSource(Obfuscator.class).toFile());
        chooser.setTitle(title);
        return chooser;
    }


    public static DirectoryChooser newDirChooser(String title) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(getCodeSource(Obfuscator.class).toFile());
        chooser.setTitle(title);
        return chooser;
    }

    public static ByteArrayOutputStream newByteArrayOutput() {
        return new ByteArrayOutputStream();
    }

    public static byte[] read(InputStream input) throws IOException {
        try (ByteArrayOutputStream output = newByteArrayOutput()) {
            transfer(input, output);
            return output.toByteArray();
        }
    }

    public static int transfer(InputStream input, OutputStream output) throws IOException {
        int transferred = 0;
        byte[] buffer = newBuffer();
        for (int length = input.read(buffer); length >= 0; length = input.read(buffer)) {
            output.write(buffer, 0, length);
            transferred += length;
        }
        return transferred;
    }

    public static ZipEntry newZipEntry(String name) {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(0);
        return entry;
    }

    public static byte[] newBuffer() {
        return new byte[BUFFER_SIZE];
    }

    public static Path getCodeSource(Class<?> clazz) {
        return Paths.get(toURI(clazz.getProtectionDomain().getCodeSource().getLocation()));
    }

    public static URI toURI(URL url) {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
