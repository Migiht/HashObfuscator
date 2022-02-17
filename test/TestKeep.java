import by.m1ght.Obfuscator;
import by.m1ght.asm.common.ObfRemapper;
import by.m1ght.config.Config;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class TestKeep {

    public static void main(String[] args) throws Throwable {
        unzip("out/Test.jar");
    }

    public  static  final void unzip(String filename) throws java.io.IOException{
        FileInputStream fis = new FileInputStream(filename);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry;
        try {
            while ((entry = zis.getNextEntry()) != null) {
                System.out.println("Extracting: " + entry);
                int count;
                byte data[] = new byte[1024];
                // Write the files to the disk

                System.out.println(entry.getSize());
                if (entry.isDirectory()) {
                    new File(entry.getName()).mkdir();
                } else {

                }

                while ((count = zis.read(data, 0, 1024)) != -1) {
                    System.out.println(Arrays.toString(data));
                }

                System.out.println(count);

                zis.closeEntry();
            }
        } finally {
            zis.close();
        }
    }
}
