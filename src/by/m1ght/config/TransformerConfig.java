package by.m1ght.config;

import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class TransformerConfig {
    public boolean enabled = false;
    public Map<String, String> params = new HashMap<>();
    //Using only for gson parser, after clear
    public Map<String, JsonElement> exclusions = new HashMap<>();

    public transient Map<Pattern, RuntimeElement> runtimeExclusions = new Object2ObjectArrayMap<>(0);

    public void applyConfig(TransformerConfig other) {
        this.enabled = other.enabled;
        this.params.putAll(other.params);
        this.runtimeExclusions.putAll(other.runtimeExclusions);
    }

    public void computeExcludes() {
        for (Map.Entry<String, JsonElement> entry : exclusions.entrySet()) {
            JsonElement value = entry.getValue();
            runtimeExclusions.put(Pattern.compile(entry.getKey()), new RuntimeElement(value.getAccess(), Pattern.compile(value.name), Pattern.compile(value.desc), value.getInterfaceExcludes()))
        }
    }

    public boolean canTransformPackage(String pkg) {
        for (Pattern pattern : runtimeExclusions.keySet()) {
            return !pattern.matcher(pkg).matches();
        }
        return true;
    }

    public boolean canTransform(String name, String desc, int access, List<AnnotationNode> anno) {
        for (RuntimeElement value : runtimeExclusions.values()) {
            return (value.access & access) == value.access &&
                    value.name.matcher(name).matches() &&
                    value.desc.matcher(desc).matches() &&
                    anno.c
        }
    }
}
