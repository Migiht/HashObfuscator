package by.m1ght.config;

import java.util.List;
import java.util.regex.Pattern;

public class RuntimeElement {
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
