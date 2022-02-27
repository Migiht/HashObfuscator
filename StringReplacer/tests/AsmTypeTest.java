import org.objectweb.asm.Type;

public class AsmTypeTest {
    public static void main(String [] args) {
        System.out.println(Type.getReturnType("([B)V").getClassName());
        for (Type argumentType : Type.getArgumentTypes("([B)V")) {
            System.out.println(argumentType.getClassName());
        }
        System.out.println(Type.getType("Ljava/util/List;").getClassName());

    }
}
