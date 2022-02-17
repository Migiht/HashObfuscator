
import by.m1ght.util.UniqueStringGenerator;
import by.m1ght.util.Util;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashBigSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Test {
    private static List<List<String>> map = new ArrayList<>();
    private static LongSet set = new LongOpenHashBigSet(Short.MAX_VALUE << 4, 0.9f);
    private static IntSet iset = new IntOpenHashSet();
    private static long superSecret = ThreadLocalRandom.current().nextLong();

    private static long hash(char[] chars) {
        long hash = 0;
        if (chars.length > 0) {

            for (int i = 0; i < chars.length; i++) {
                hash = 127 * hash + chars[i];
            }
        }
        return hash ;
    }

    private static int hashI(char[] chars) {
        //System.out.println(chars);
        int hash = 0;
        if (chars.length > 0) {

            for (int i = 0; i < chars.length; i++) {
                hash = 127 * hash + chars[i];
            }
        }
        return hash;
    }

    private static void check(char[] chars) {
        if (false) {
            System.out.println(chars);
        }
    }

    private static void check(int code) {
        if (iset.contains(code)) {
            System.out.println("durka F ");
        } else {
            iset.add(code);
        }
    }

    private static void check(long code) {
        if (set.contains(code)) {
            System.out.println("durka F ");
        } else {
            set.add(code);
        }
    }

    public static void main(String[] args) throws Throwable {
        System.out.println(0b10000000000000000000000000000000 << 1);

     /*   for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if ((i ^ 3124123) == 3124123) System.out.println("ebat");
        }
        System.out.println(UniqueStringGenerator.get(0L));
        System.out.println(UniqueStringGenerator.get(1L));
        System.out.println(UniqueStringGenerator.get(2L));
        System.out.println(UniqueStringGenerator.get(3L));
        System.out.println(UniqueStringGenerator.get(Long.MAX_VALUE - 1));
        System.out.println(UniqueStringGenerator.get(Long.MAX_VALUE));
        System.out.println(UniqueStringGenerator.get(Long.MAX_VALUE + 1));
        System.out.println(UniqueStringGenerator.get(Long.MIN_VALUE));
        System.out.println(UniqueStringGenerator.get(0));
        System.out.println(UniqueStringGenerator.get(Long.MIN_VALUE + 2));
*/
        //HARD GENERATOR TEST

        for (int i = 0; i < 100_000; i++) {
            String value = "run(Lmindustry/gen/LegsUnitLegacyToxopid;FLmindustry/entities/bullet/BulletType;Lmindustry/type/Weapon;FFLmindustry/entities/units/WeaponMount;)Ljava/lang/Runnable;" + UniqueStringGenerator.get(i) + i;
            check(Util.hash(value, String.valueOf(i)));
        }
    }
}
