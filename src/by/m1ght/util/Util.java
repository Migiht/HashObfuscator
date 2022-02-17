package by.m1ght.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;

public final class Util {
    private static Unsafe unsafe;
    private static long strValueOffset;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
            strValueOffset = unsafe.objectFieldOffset(String.class.getDeclaredField("value"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static char[] getStringChars(String src) {
        return (char[]) unsafe.getObject(src, strValueOffset);
    }

    public static long hash(String s1) {
        return hash(0, s1);
    }

    public static long hash(String s1, String s2) {
        return hash(hash(0, s1), s2);
    }

    public static long hash(String s1, String s2, String s3) {
        return hash(hash(hash(0, s1), s2), s3);
    }

    public static long hash(long offset, String s1) {
        char[] chars = Util.getStringChars(s1);
        if (chars.length > 0) {
            int length = chars.length;
            for (int i = 0; i < length; ) {
                offset = Byte.MAX_VALUE * offset + chars[i];
                ++i;
            }
        }
        return (offset);
    }

    public static long hashChar(long offset, char symbol) {
        offset = Byte.MAX_VALUE * offset;
        offset += symbol;
        return offset;
    }

    public static String setNewName(String src, String newName) {
        int index = src.lastIndexOf('/');

        String result;
        if (index != -1) {
            result = src.substring(0, index + 1) + newName;
        } else {
            result = newName;
        }
        return result;
    }

    public static String setNewPackage(String src, String newPackage) {
        int index = src.lastIndexOf('/');

        String result;

        if (newPackage.length() > 0) {
            newPackage = newPackage + '/';
        }

        if (index != -1) {
            result = newPackage + src.substring(index + 1);
        } else {
            result = newPackage + src;
        }
        return result;
    }

    public static void crashCRC(ZipOutputStream stream) {
        try {
            Field field = ZipOutputStream.class.getDeclaredField("crc");
            field.setAccessible(true);
            field.set(stream, new CRC32() {

                @Override
                public void update(byte[] b, int off, int len) {
                    // Не обновляем CRC
                }

                @Override
                public long getValue() {
                    return 2022;
                }

            });
        } catch (Throwable throwable) {
            LogUtil.error("Ошибка при изменении CRC");
        }
    }

    public static List<String> getDefaultJreLibs() {
        List<String> paths = new ArrayList<>();
        if (System.getProperty("java.home") != null) {
            Path home = Paths.get(System.getProperty("java.home")).resolve("lib");

            Path rt = home.resolve("rt.jar");
            if (Files.exists(rt)) {
                paths.add(rt.toString());
            }

            Path jce = home.resolve("jce.jar");
            if (Files.exists(jce)) {
                paths.add(jce.toString());
            }
        }
        return paths;
    }

    public static <T> T fromJson(GsonBuilder builder, Path file, T object) {
        try {
            return builder
                    .serializeNulls()
                    .setPrettyPrinting()
                    .enableComplexMapKeySerialization()
                    .create()
                    .fromJson(new String(Files.readAllBytes(file), StandardCharsets.UTF_8), (Class<T>) object.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    public static <T> T fromJson(Path file, T object) {
        return fromJson(new GsonBuilder(), file, object);
    }

    public static <T> void writeJson(Path file, T object) {
        writeJson(new GsonBuilder(), file, object);
    }

    public static <T> void writeJson(GsonBuilder builder, Path file, T object) {
        String json = builder
                .serializeNulls()
                .setPrettyPrinting()
                .enableComplexMapKeySerialization()
                .create().toJson(object);
        try {
            IOUtil.write(file, json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFlag(int id, int access) {
        return (id & access) != 0;
    }

    public static boolean noFlag(int id, int access) {
        return (id & access) == 0;
    }

    public static int setFlag(int src, int dst) {
        return src | dst;
    }

    public static int getFirstInt(long src) {
        return (int) (src >>> 32);
    }

    public static int getSecondInt(long src) {
        return (int) (src);
    }
}
