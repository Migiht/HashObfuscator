package by.m1ght.asm.common;

import by.m1ght.GeneratorType;
import by.m1ght.util.UniqueStringGenerator;
import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;

import java.util.concurrent.atomic.AtomicInteger;

public class ObfHasher {
    private final ObfRemapper mapper;
    private final Long2ObjectMap<String> cache = new Long2ObjectOpenHashMap<>();
    private final AtomicInteger seqId = new AtomicInteger();
    private final long secret;
    public GeneratorType type = GeneratorType.NAME_DESC_HASH;

    public ObfHasher(ObfRemapper mapper, long secret) {
        this.mapper = mapper;
        this.secret = secret;
    }

    public void setType(String type) {
        if (type != null) {
            this.type = GeneratorType.valueOf(type);
        }
    }

    public long hash(long offset, String s1) {
        return Util.hash(offset, s1) ^ secret;
    }

    public long hash(String s1) {
        return hash(0, s1);
    }

    public long hash(String s1, String s2) {
        return hash(hash(0, s1), s2);
    }

    public long hash(String s1, String s2, String s3) {
        return hash(hash(hash(0, s1), s2), s3);
    }

    public void putINDY(String owner, String name, String desc, InvokeDynamicInsnNode node) {
        putIfFound(hash(name, desc, owner), hash(node.name, node.desc));
    }

    public void putReplaceOwner(String owner, String newOwner, String name, String desc) {
        long nameDescHash = hash(name, desc);
        long oldHash = hash(nameDescHash, owner);
        long newHash = hash(nameDescHash, newOwner);

        putIfFound(oldHash, newHash);
    }

    public void putIfFound(long findKey, long key) {
        String found = cache.get(findKey);

        if (found != null) {
            cache.put(key, found);
        }
    }

    public void put(String owner, String name, String desc) {
        long nameHash = hash(name);
        long nameDescHash = hash(nameHash, desc);
        long total = hash(nameDescHash, owner);

        String generated = null;

        switch (type) {

            case SEQUENCE:
                generated = cache.computeIfAbsent(nameDescHash, (func) -> UniqueStringGenerator.get(seqId.getAndIncrement()));
                break;

            case NAME_HASH:
                generated = UniqueStringGenerator.get(nameHash);
                mapper.putLDC(name, generated);
                break;

            case NAME_DESC_HASH:
                generated = UniqueStringGenerator.get(nameDescHash);
                break;

        }

        cache.put(total, generated);
    }

    public String remap(String name, String desc) {
        return cache.getOrDefault(hash(name, desc), name);
    }

    public String remap(String name, String desc, String owner) {
        return cache.getOrDefault(hash(name, desc, owner), name);
    }

}
