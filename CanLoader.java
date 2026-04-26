import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * CanLoader.java
 * Resolves can PNG paths reliably whether running from IDE, terminal, or JAR.
 * Put the "cans" folder next to your .class files or project root.
 */
public class CanLoader {

    private static final String[] FILE_NAMES = {
        "BlueBull.png", "Moonbucks.png", "Latorade.png", "PhDPepper.png", "Kelvin.png"
    };

    /**
     * Loads all 5 can images. Tries multiple path strategies so it works
     * from any working directory or IDE project structure.
     */
    public static BufferedImage[] loadAll() {
        BufferedImage[] images = new BufferedImage[5];
        for (int i = 0; i < FILE_NAMES.length; i++) {
            images[i] = load(FILE_NAMES[i]);
            if (images[i] == null) {
                System.err.println("[CanLoader] MISSING: " + FILE_NAMES[i]
                    + " — searched in: " + searchPaths(FILE_NAMES[i]));
            } else {
                System.out.println("[CanLoader] Loaded: " + FILE_NAMES[i]);
            }
        }
        return images;
    }

    private static BufferedImage load(String fileName) {
        // Strategy 1: cans/ relative to working directory
        File f1 = new File("cans/" + fileName);
        if (f1.exists()) return readQuiet(f1);

        // Strategy 2: cans/ relative to the .class file location
        try {
            URL classUrl = CanLoader.class.getProtectionDomain().getCodeSource().getLocation();
            File classDir = new File(classUrl.toURI()).getParentFile();
            File f2 = new File(classDir, "cans/" + fileName);
            if (f2.exists()) return readQuiet(f2);
        } catch (Exception ignored) {}

        // Strategy 3: absolute path with user.dir
        File f3 = new File(System.getProperty("user.dir") + "/cans/" + fileName);
        if (f3.exists()) return readQuiet(f3);

        // Strategy 4: one level up (src/.. style project)
        File f4 = new File("../cans/" + fileName);
        if (f4.exists()) return readQuiet(f4);

        // Strategy 5: classpath resource
        InputStream is = CanLoader.class.getResourceAsStream("/cans/" + fileName);
        if (is == null) is = CanLoader.class.getResourceAsStream("cans/" + fileName);
        if (is != null) {
            try { return ImageIO.read(is); } catch (Exception ignored) {}
        }

        return null;
    }

    private static BufferedImage readQuiet(File f) {
        try { return ImageIO.read(f); } catch (Exception e) { return null; }
    }

    private static String searchPaths(String fileName) {
        return "\n  1) " + new File("cans/" + fileName).getAbsolutePath()
             + "\n  2) " + new File(System.getProperty("user.dir") + "/cans/" + fileName).getAbsolutePath()
             + "\n  → Make sure the 'cans' folder is in: " + System.getProperty("user.dir");
    }
}