package mod.seanld.rawinput.input;

import java.io.IOException;

public interface MouseBackend extends AutoCloseable {
    void poll(double[] motion) throws IOException;

    @Override
    void close();
}
