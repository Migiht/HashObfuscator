package by.m1ght.start;

import by.m1ght.Obfuscator;
import by.m1ght.config.Config;
import by.m1ght.util.LogUtil;
import by.m1ght.util.Util;

import java.nio.file.Paths;

public class Starter
{
    static {
        Config config = new Config();
        try {
            config = Util.fromJson(Paths.get("obf_cfg.json"), config);
        } catch (Throwable exception) {
            LogUtil.error("Cannot load config, check example -> " + Paths.get("obf_cfg.json").toAbsolutePath());
            Util.writeJson(Paths.get("config.json"), config);
            System.exit(-1);
        }
        new Obfuscator(config).run();
    }
}
