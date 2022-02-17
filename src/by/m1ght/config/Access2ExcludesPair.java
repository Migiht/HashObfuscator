package by.m1ght.config;

import by.m1ght.util.AsmUtil;
import by.m1ght.util.LogUtil;
import it.unimi.dsi.fastutil.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Access2ExcludesPair implements Pair<String, String> {
    private final String access;
    private final String excludes;

    public Access2ExcludesPair(String superNames, String annotations) {
        this.access = superNames;
        this.excludes = annotations;
    }

    public int getAccess() {
        int result = 0;
        for (String keyWord : access.split(" ")) {
            result += AsmUtil.parseKeyWordToAccess(keyWord.toLowerCase(Locale.ROOT));
        }
        LogUtil.info("Compute access " + result + " from " + access);
        return result;
    }

    public List<String> getExcludes() {
        return Arrays.stream(excludes.split(" ")).map(s -> s.replace("@", "")).map(s -> 'L' + s + ';').collect(Collectors.toList());
    }

    @Override
    public String left() {
        return access;
    }

    @Override
    public String right() {
        return excludes;
    }
}
