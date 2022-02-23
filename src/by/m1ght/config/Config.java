package by.m1ght.config;

import by.m1ght.transformer.TransformerType;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.*;

public final class Config {
    // Additionally
    public boolean crashCRC;
    public boolean debug;
    public boolean caching; //More memory but lowest CPU usage

    // Files
    public String inputPath = "Updater.jar";
    public String outputPath = "output.jar";

    // Libs
    public Set<String> libs = new HashSet<>();

    public final Map<TransformerType, TransformerConfig> transformerConfigMap = new Object2ObjectArrayMap<>();

    public Config() {
        for (TransformerType value : TransformerType.values()) {
            transformerConfigMap.put(value, new TransformerConfig());
        }
    }

    public void computeExcludes() {
        for (Map.Entry<TransformerType, TransformerConfig> entry : transformerConfigMap.entrySet()) {
            entry.getValue().computeExcludes();
        }
    }
}
