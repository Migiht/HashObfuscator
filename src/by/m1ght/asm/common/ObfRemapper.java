package by.m1ght.asm.common;

import by.m1ght.Obfuscator;
import by.m1ght.util.AsmUtil;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.commons.Remapper;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ObfRemapper extends Remapper {
    protected final Obfuscator obfuscator;

    private final long secret = HashCommon.murmurHash3(ThreadLocalRandom.current().nextLong());

    public final ObfHasher methodHasher = new ObfHasher(this, secret);
    public final ObfHasher fieldHasher = new ObfHasher(this, secret);

    private final Object2ObjectMap<String, String> classNames = new Object2ObjectOpenHashMap<>();

    private final Map<String, String> LDCNames = new Object2ObjectOpenHashMap<>();

    public ObfRemapper(Obfuscator obfuscator) {
        this.obfuscator = obfuscator;
    }

    public void putLDC(String string) {
        LDCNames.putIfAbsent(string, null);
    }

    public void replaceLDC(String old, String newName) {
        if (!LDCNames.isEmpty()) {
            if (LDCNames.replace(old, newName) == null) {
                LDCNames.replace(AsmUtil.getAsmName(old), newName);
            }
        }
    }

    public Object mapLDC(Object original) {
        if (original instanceof String) {
            String result = LDCNames.get(original);
            if (result != null) {
                return result;
            }
        }
        return original;
    }

    public void putClassName(String old, String newName) {
        classNames.put(old, newName);

        replaceLDC(old, newName);
    }

    public String mapMethodName(String owner, String name, String descriptor) {
        return methodHasher.remap(name, descriptor, owner);
    }

    public String mapFieldName(String owner, String name, String descriptor) {
        return fieldHasher.remap(name, descriptor, owner);
    }

    public String mapInvokeDynamicMethodName(String name, String descriptor) {
        return methodHasher.remap(name, descriptor);
    }

    public String mapAnnotationAttributeName(String descriptor, String name) {
        return name;
    }

    public String map(String key) {
        return classNames.get(key);
    }

    public Map<String, String> getClassNames() {
        return Object2ObjectMaps.unmodifiable(classNames);
    }

    @Override
    public String toString() {
        return "ObfRemapper{" +
                "classNames=" + classNames +
                '}';
    }
}
