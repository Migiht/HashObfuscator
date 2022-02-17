package by.m1ght.start;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    private static void start(List<Path> paths) {
        try {
            List<URL> links = new ArrayList<>();
            for (Path path : paths) {
                links.add(path.toUri().toURL());
            }
            if (Thread.currentThread().getContextClassLoader() instanceof URLClassLoader) {
                links.addAll(Arrays.asList(((URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs()));
            }
            URLClassLoader newLoader = new URLClassLoader(links.toArray(new URL[0]));

            newLoader.loadClass(Starter.class.getName()).newInstance();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static List<Path> getCP(Path path) throws Throwable {
        return Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toList());
    }

    public static void main(String[] args) throws Throwable {
        if (args.length > 0) {
            start(getCP(Paths.get(args[0])));
        }
    }
}
