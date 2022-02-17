import javax.swing.*;
import java.net.URI;
import java.nio.ByteBuffer;

public class Main {
    public Main() {
        JOptionPane.showMessageDialog(null, "Fuck U");
    }

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        System.out.println(buffer.array().length);
    }
}
