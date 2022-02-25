package by.m1ght.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class RuntimeExclusions {
    public final Map<Pattern, List<String>> classExcludes = new Object2ObjectArrayMap<>();
    public final Map<Pattern, List<RuntimeElement>> methodExclusions = new Object2ObjectArrayMap<>();
    public final Map<Pattern, List<RuntimeElement>> fieldExclusions = new Object2ObjectArrayMap<>();

    public void merge(RuntimeExclusions exclusions) {
        this.classExcludes.putAll(exclusions.classExcludes);
        this.methodExclusions.putAll(exclusions.methodExclusions);
        this.fieldExclusions.putAll(exclusions.fieldExclusions);
    }

    public static class RuntimeElement {
        public final int access;
        public final Pattern name;
        public final Pattern desc;
        public final List<String> interfaceExcludes;

        public RuntimeElement(int access, Pattern name, Pattern desc, List<String> interfaceExcludes) {
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.interfaceExcludes = interfaceExcludes;
        }
    }
}
