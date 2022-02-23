package by.m1ght.config;

import by.m1ght.util.AsmUtil;
import by.m1ght.util.LogUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class JsonElement {
    public final String access;
    public final String name;
    public final String desc;
    public final String[] interfaceExcludes;

    public JsonElement(String access, String name, String desc, String[] interfaceExcludes) {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.interfaceExcludes = interfaceExcludes;
    }

    public int getAccess() {
        int result = 0;
        for (String keyWord : access.split(" ")) {
            result += AsmUtil.parseKeyWordToAccess(keyWord.toLowerCase(Locale.ROOT));
        }
        LogUtil.info("Compute access " + result + " from " + access);
        return result;
    }

    public List<String> getInterfaceExcludes() {
        return Arrays.stream(interfaceExcludes).map(s -> s.replace("@", "")).map(s -> 'L' + s + ';').collect(Collectors.toList());
    }
}
