package by.gdev.util.os.additional;

import java.io.IOException;
import java.nio.file.Path;

import by.gdev.util.os.OSExecutor;

public interface OSExecutorAdditional extends OSExecutor{
	/**
     * @return system switches off power computer after some time
     */
    int getSystemHibernateDelay();

    /**
     * should be bind to one thread
     *
     * @param seconds time without any events
     */
    boolean isIdleWithoutInputEventsMoreThan(int seconds);

    /**
     * should be bind to one thread
     *
     * @param seconds time without any events
     */
    boolean isIdleWithoutExecutionStateMoreThan(int seconds);

    /**
     * should be bind to one thread
     */
    int setThreadExecutionState(int code);
    
    /**
     * @param startUpAppPath ran file
     * @param folder         - app folder to run
     * @param name           - name of the running config
     */
    void startUpAppWithSystem(Path startUpAppPath, Path folder, String name) throws IOException;

    void deactivateStartupAppWithSystem(String name) throws IOException;

}
