package by.m1ght.config;

import by.m1ght.util.AsmUtil;
import by.m1ght.util.LogUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonExclusions {
    public final Map<String, List<String>> classExcludes = new Object2ObjectArrayMap<>();
    public final Map<String, List<JsonElement>> methodExclusions = new Object2ObjectArrayMap<>();
    public final Map<String, List<JsonElement>> fieldExclusions = new Object2ObjectArrayMap<>();

    public RuntimeExclusions compute() {
        RuntimeExclusions instance = new RuntimeExclusions();
        for (Map.Entry<String, List<String>> entry : classExcludes.entrySet()) {
            instance.classExcludes.put(Pattern.compile(entry.getKey()), AsmUtil.toAsmNames(entry.getValue()));
        }

        for (Map.Entry<String, List<JsonElement>> entry : methodExclusions.entrySet()) {
            instance.methodExclusions.put(Pattern.compile(entry.getKey()), entry.getValue().stream().map(JsonElement::compute).collect(Collectors.toList()));
        }

        for (Map.Entry<String, List<JsonElement>> entry : fieldExclusions.entrySet()) {
            instance.fieldExclusions.put(Pattern.compile(entry.getKey()), entry.getValue().stream().map(JsonElement::compute).collect(Collectors.toList()));
        }
        return instance;
    }


    public static class JsonElement {
        public final String access;
        public final String name;
        public final String desc;
        public final List<String> interfaceExcludes;

        public JsonElement(String access, String name, String desc, List<String> interfaceExcludes) {
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.interfaceExcludes = interfaceExcludes;
        }

        public RuntimeExclusions.RuntimeElement compute() {
            return new RuntimeExclusions.RuntimeElement(computeAccess(), Pattern.compile(name), Pattern.compile(desc), AsmUtil.toAsmNames(interfaceExcludes));
        }

        public int computeAccess() {
            int result = 0;
            for (String keyWord : access.split(" ")) {
                result += AsmUtil.parseKeyWordToAccess(keyWord.toLowerCase(Locale.ROOT));
            }
            LogUtil.info("Compute access " + result + " from " + access);
            return result;
        }
    }
}
