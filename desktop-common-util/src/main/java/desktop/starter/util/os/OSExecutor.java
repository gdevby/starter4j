package desktop.starter.util.os;

import desktop.starter.util.model.CUDAVersion;
import desktop.starter.util.model.GPUsDescriptionDTO;

import java.io.IOException;

/**
 * Used unique methods or info to get info about every os.
 */
public interface OSExecutor {
    String execute(String command, int seconds) throws IOException, InterruptedException;

    GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException;

    CUDAVersion getCUDAVersion() throws IOException;


}
