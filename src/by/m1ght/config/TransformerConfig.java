package by.m1ght.config;

import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class TransformerConfig {
    public boolean enabled = false;
    public Map<String, String> params = new HashMap<>();
    //Using only for gson parser, after clear
    public JsonExclusions jsonExclusions = new JsonExclusions();

    public transient RuntimeExclusions runtimeExclusions;

    public void applyConfig(TransformerConfig other) {
        this.enabled = other.enabled;
        this.params.putAll(other.params);
        this.runtimeExclusions.merge(other.runtimeExclusions);
    }

    public void computeExcludes() {
        runtimeExclusions = jsonExclusions.compute();
    }

    public boolean canTransform(ClassNode node) {
        for (Map.Entry<Pattern, List<String>> entry : runtimeExclusions.classExcludes.entrySet()) {
            boolean exclude = entry.getKey().matcher(node.name).matches();

            if (exclude) {
                if (entry.getValue().isEmpty()) {
                    return false;
                }

                for (AnnotationNode anno : node.visibleAnnotations) {
                    if (entry.getValue().contains(anno.desc)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean canTransformField(String owner, String name, String desc, int access, List<AnnotationNode> list) {
        for (Map.Entry<Pattern, List<RuntimeExclusions.RuntimeElement>> entry : runtimeExclusions.fieldExclusions.entrySet()) {
            boolean exclude = entry.getKey().matcher(owner).matches();

            if (exclude) {
                for (RuntimeExclusions.RuntimeElement element : entry.getValue()) {
                    if ((element.access & access) == element.access && element.name.matcher(name).matches() && element.desc.matcher(desc).matches()) {
                        if (entry.getValue().isEmpty()) {
                            return false;
                        }

                        for (AnnotationNode anno : list) {
                            if (element.interfaceExcludes.contains(anno.desc)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean canTransformMethod(String owner, String name, String desc, int access, List<AnnotationNode> anno) {
        for (RuntimeElement value : runtimeExclusions.values()) {
            boolean exclude = (value.access & access) == value.access &&
                    value.name.matcher(name).matches() &&
                    value.desc.matcher(desc).matches();

            if (anno.isEmpty()) return !exclude;
            for (AnnotationNode annotationNode : anno) {
                if (value.interfaceExcludes.contains(annotationNode.desc)) {
                    return !exclude;
                }
            }
        }
        return true;
    }
}
