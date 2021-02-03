package desktop.starter.util.os;

import desktop.starter.util.model.CUDAVersion;
import desktop.starter.util.model.GPUsDescriptionDTO;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Used unique methods or info to get info about every os.
 */
public interface OSExecutor {
    String execute(String command, int seconds) throws IOException, InterruptedException;

    GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException;

    CUDAVersion getCUDAVersion() throws IOException;

    /**
     * @return system switches off power computer after some time
     * @throws IOException
     */
    int getSystemHibernateDelay();

    /**
     * should be bind to one thread
     *
     * @param seconds time without any events
     * @return
     */
    boolean isIdleWithoutInputEventsMoreThan(int seconds);

    /**
     * should be bind to one thread
     *
     * @param seconds time without any events
     * @return
     */
    boolean isIdleWithoutExecutionStateMoreThan(int seconds);

    /**
     * should be bind to one thread
     *
     * @param code
     * @return
     */
    int setThreadExecutionState(int code);


    /**
     * @param startUpAppPath ran file
     * @param folder         - app folder to run
     * @param name           - name of the running config
     * @return
     * @throws IOException
     */
    void startUpAppWithSystem(Path startUpAppPath, Path folder, String name) throws IOException;

    void deactivateStartupAppWithSystem(String name) throws IOException;
}
