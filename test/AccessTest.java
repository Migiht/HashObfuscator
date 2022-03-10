import by.m1ght.transformer.Transformer;
import by.m1ght.util.AsmUtil;
import by.m1ght.util.Util;
import org.objectweb.asm.Opcodes;

public class AccessTest {
    public static void main(String[] args) throws Throwable {
        System.out.println(AsmUtil.toAsmName(Object.class.getName()));
    }
}
