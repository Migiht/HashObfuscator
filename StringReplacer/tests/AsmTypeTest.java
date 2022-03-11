import org.objectweb.asm.Type;

public class AsmTypeTest {
    public static void main(String [] args) {
        System.out.println(Type.getReturnType("posis([B)V").getClassName());
        System.out.println(splitName("posis([B)V"));
    }


    public static String splitName(String nameDesc) {
        return nameDesc.substring(0, nameDesc.indexOf('('));
    }
}
