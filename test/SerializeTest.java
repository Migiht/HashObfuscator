import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;

import java.nio.file.Paths;

public class SerializeTest {
    public static void main(String[] args) {
        Util.writeJson(Paths.get("").resolve("123.json"), new ObjectObjectImmutablePair<>("fw", "f"));
    }
}
