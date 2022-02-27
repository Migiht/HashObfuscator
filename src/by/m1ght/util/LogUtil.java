package by.m1ght.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class LogUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss", Locale.ROOT);
    private static final List<Output> OUTPUTS = new CopyOnWriteArrayList<>();
    private static volatile boolean debug = false;

    static {
        OUTPUTS.add(System.out::println);
    }

    public static boolean isDebugEnabled() {
        return debug;
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        debug = debugEnabled;
    }

    public static void println(String message) {
        for (Output output : OUTPUTS) {
            output.println(message);
        }
    }

    public static void addOutput(Output output) {
        OUTPUTS.add(Objects.requireNonNull(output, "output"));
    }

    public static void removeOutput(Output output) {
        OUTPUTS.remove(output);
    }

    public static void debug(String message) {
        if (isDebugEnabled()) {
            log(Level.DEBUG, message);
        }
    }

    public static void debug(String message, Object... args) {
        debug(String .format(message, args));
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    public static void error(String message, Object... args) {
        error(String.format(message, args));
    }

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void info(String message, Object... args) {
        info(String.format(message, args));
    }

    public static void warning(String message) {
        log(Level.WARNING, message);
    }

    public static void warning(String message, Object... args) {
        warning(String.format(message, args));
    }

    public static void log(Level level, String message) {
        String dateTime = DATE_TIME_FORMATTER.format(LocalDateTime.now());
        println(formatLog(level, message, dateTime));
    }

    private static String formatLog(Level level, String message, String dateTime) {
        return dateTime + " [" + level + "] " + message;
    }

    public enum Level {
        DEBUG,
        INFO,
        WARNING,
        ERROR
        ;
    }

    @FunctionalInterface
    public interface Output {
        void println(String message);
    }
}
