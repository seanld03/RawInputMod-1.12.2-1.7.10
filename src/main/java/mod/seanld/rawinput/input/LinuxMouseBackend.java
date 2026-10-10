package mod.seanld.rawinput.input;

import java.awt.AWTError;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class LinuxMouseBackend implements MouseBackend {
    private static boolean loaded;
    private long handle;

    public LinuxMouseBackend() throws IOException {
        loadLibrary();
        try {
            Toolkit.getDefaultToolkit();
        } catch (AWTError e) {
            throw new IOException("Unable to initialize AWT input", e);
        }
        handle = open(findAWTLibrary());
    }

    private static String architecture() {
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        if (arch.equals("x86_64")) {
            return "amd64";
        }
        if (arch.equals("arm64")) {
            return "aarch64";
        }
        return arch;
    }

    private static String findAWTLibrary() throws IOException {
        Path javaHome = Paths.get(System.getProperty("java.home"));
        Path library = javaHome.resolve("lib").resolve(architecture()).resolve("libjawt.so");
        if (!Files.isRegularFile(library)) {
            library = javaHome.resolve("lib").resolve("libjawt.so");
        }
        if (!Files.isRegularFile(library)) {
            throw new IOException("Unable to locate the AWT input library");
        }
        return library.toAbsolutePath().toString();
    }

    private static synchronized void loadLibrary() throws IOException {
        if (loaded) {
            return;
        }
        String arch = architecture();
        String resource = "/mod/seanld/rawinput/natives/linux-" + arch + "/librawinput.so";
        try (InputStream stream = LinuxMouseBackend.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IOException("No Linux raw input library for " + arch);
            }
            Path library = Files.createTempFile("rawinput-", ".so");
            library.toFile().deleteOnExit();
            Files.copy(stream, library, StandardCopyOption.REPLACE_EXISTING);
            System.load(library.toAbsolutePath().toString());
            loaded = true;
        }
    }

    @Override
    public void poll(double[] motion) throws IOException {
        poll(handle, motion);
    }

    @Override
    public void close() {
        if (handle != 0) {
            close(handle);
            handle = 0;
        }
    }

    private static native long open(String awtLibrary) throws IOException;

    private static native void poll(long handle, double[] motion) throws IOException;

    private static native void close(long handle);
}
