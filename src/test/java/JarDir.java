import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by IntelliJ IDEA.
 * User: thebaz
 * Date: 3/4/12
 * Time: 5:17 PM
 */
public class JarDir {
    public static void main (String args[])
            throws IOException {
        if (args.length != 1) {
            System.out.println("Please provide a JAR filename");
            System.exit(-1);
        }
        JarFile jarFile = new JarFile(args[0]);
        Enumeration enumeration = jarFile.entries();
        while (enumeration.hasMoreElements()) {
            process(enumeration.nextElement());
        }
    }

    private static void process(Object obj) {
        JarEntry entry = (JarEntry)obj;
        String name = entry.getName();
        long size = entry.getSize();
        long compressedSize = entry.getCompressedSize();
        System.out.println(name + "\t" + size + "\t" + compressedSize);
    }
}
