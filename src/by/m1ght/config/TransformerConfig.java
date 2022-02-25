package by.m1ght.config;

import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

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

    public boolean canTransformField(ClassNode owner, FieldNode node) {
        for (Map.Entry<Pattern, List<RuntimeExclusions.RuntimeElement>> entry : runtimeExclusions.fieldExclusions.entrySet()) {
            boolean exclude = entry.getKey().matcher(owner.name).matches();

            if (exclude) {
                for (RuntimeExclusions.RuntimeElement element : entry.getValue()) {
                    if ((element.access & node.access) == element.access && element.name.matcher(node.name).matches() && element.desc.matcher(node.desc).matches()) {
                        if (entry.getValue().isEmpty()) {
                            return false;
                        }

                        for (AnnotationNode anno : node.visibleAnnotations) {
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

    public boolean canTransformMethod(ClassNode owner, MethodNode node) {
        for (Map.Entry<Pattern, List<RuntimeExclusions.RuntimeElement>> entry : runtimeExclusions.methodExclusions.entrySet()) {
            boolean exclude = entry.getKey().matcher(owner.name).matches();

            if (exclude) {
                for (RuntimeExclusions.RuntimeElement element : entry.getValue()) {
                    if ((element.access & node.access) == element.access && element.name.matcher(node.name).matches() && element.desc.matcher(node.desc).matches()) {
                        if (entry.getValue().isEmpty()) {
                            return false;
                        }

                        for (AnnotationNode anno : node.visibleAnnotations) {
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
}
