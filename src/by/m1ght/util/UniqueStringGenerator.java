package by.m1ght.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class UniqueStringGenerator {
    private static boolean caching;
    private static final char[] CHARSET_LOW = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] CHARSET_UP = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int TEMP_ARRAY_SIZE = 16;
    private static final Long2ObjectMap<String> cache = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

    public static void setCaching(boolean caching) {
        LogUtil.info("Generator caching: " + caching);
        UniqueStringGenerator.caching = caching;
    }

    public static String get(int index) {
        if (caching) {
            return cache.computeIfAbsent(index, (key) -> next0(index));
        }
        return next0(index);
    }

    public static String get(long index) {
        if (caching) {
            return cache.computeIfAbsent(index, (key) -> next0(index));
        }
        return next0(index);
    }

    private static String next0(int i) {
        char[] buf = new char[TEMP_ARRAY_SIZE];
        boolean negative = (i < 0);
        char[] charset = negative ? CHARSET_UP : CHARSET_LOW;
        int charsetLength = charset.length;
        int charPos = TEMP_ARRAY_SIZE - 1;

        if (!negative) {
            i = -i;
        }

        while (i <= -charsetLength) {
            buf[charPos--] = charset[-(i % charsetLength)];
            i = (i / charsetLength) + 1;
        }

        buf[charPos] = charset[-i];
        return new String(buf, charPos, (TEMP_ARRAY_SIZE - charPos));
    }

    private static String next0(long i) {
        char[] buf = new char[TEMP_ARRAY_SIZE];
        boolean negative = (i < 0);
        char[] charset = negative ? CHARSET_UP : CHARSET_LOW;
        int charsetLength = charset.length;
        int charPos = TEMP_ARRAY_SIZE - 1;

        if (!negative) {
            i = -i;
        }

        while (i <= -charsetLength) {
            buf[charPos--] = charset[(int) -(i % charsetLength)];
            i = (i / charsetLength) + 1;
        }

        buf[charPos] = charset[(int) -i];
        return new String(buf, charPos, (TEMP_ARRAY_SIZE - charPos));
    }
}
