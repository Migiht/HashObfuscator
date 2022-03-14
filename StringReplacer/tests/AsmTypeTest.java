import by.m1ght.util.AsmUtil;
import org.objectweb.asm.Type;

public class AsmTypeTest {
    public static void main(String [] args) {
        System.out.println(Type.getReturnType("posis([B)V").getClassName());
        System.out.println(splitName("posis([B)V"));
        System.out.println(splitDesc("posis([B)V"));
        System.out.println(AsmUtil.getReturnType("posos(I)Lfwef;"));
}


    public static String splitName(String nameDesc) {
        return nameDesc.substring(0, nameDesc.indexOf('('));
    }

    public static String splitDesc(String nameDesc) {
        return nameDesc.substring(nameDesc.indexOf('('));
    }
}
