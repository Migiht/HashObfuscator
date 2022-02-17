package by.m1ght.config;

import by.m1ght.util.LogUtil;
import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TransformerConfig {
    public boolean enabled = false;

    //Using only for gson parser, after clear
    public Map<String, Access2ExcludesPair> exclusions = new HashMap<>();

    public transient Long2ObjectMap<IntObjectPair<ObjectSet<String>>> runtimeExclusions = new Long2ObjectOpenHashMap<>();

    public Map<String, String> params = new HashMap<>();

    public void applyConfig(TransformerConfig other) {
        this.enabled = other.enabled;
        this.params.putAll(other.params);
        this.runtimeExclusions.putAll(other.runtimeExclusions);
    }

    public String getParam(String type) {
        return params.get(type);
    }

    public TransformerConfig compute() {
        for (Map.Entry<String, Access2ExcludesPair> entry : exclusions.entrySet()) {

            if (entry.getKey().equals("*")) {
                runtimeExclusions.defaultReturnValue(computePair(entry.getValue()));
                continue;
            }

            long hash = 0;
            for (String s : entry.getKey().split("\\.")) {
                hash = Util.hash(hash, s);
            }

            runtimeExclusions.put(hash, computePair(entry.getValue()));
        }
        exclusions.clear();
        return this;
    }

    private IntObjectPair<ObjectSet<String>> computePair(Access2ExcludesPair pair) {

        int access = 0;

        ObjectSet<String> annotations = null;

        if (pair != null) {

            if (pair.left() != null && !pair.left().isEmpty()) {
                access = pair.getAccess();
            }

            if (pair.right() != null && !pair.right().isEmpty()) {
                annotations = new ObjectOpenHashSet<>(pair.getExcludes());
            }

        }

        return new IntObjectImmutablePair<>(access, annotations);
    }

    public boolean canTransformPackage(String s1) {
        if (runtimeExclusions.defaultReturnValue() == null) {
            char[] chars = Util.getStringChars(s1);
            if (chars.length > 0) {
                long offset = 0;
                for (int i = 0; i < chars.length; i++) {
                    //Scan all packages, for example java/nio/buffer scans java, than java/nio, than java/nio/buffer
                    if (chars[i] == '/' && runtimeExclusions.containsKey(offset)) {
                        return false;
                    }
                    offset = Byte.MAX_VALUE * offset + chars[i];
                }
            }
        }
        return true;
    }

    public boolean canTransform(long hash, int access, List<AnnotationNode> anno) {
        IntObjectPair<ObjectSet<String>> pair = runtimeExclusions.get(hash);

        if (pair != null) {

            if ((pair.leftInt() & access) == pair.leftInt() && pair.right() == null) {
                return false;
            }

            if (pair.right() != null) {
                for (AnnotationNode annotationNode : anno) {
                    if (pair.right().contains(annotationNode.desc)) {
                        return false;
                    }
                }
            }

            return false;
        }
        return true;
    }
}
