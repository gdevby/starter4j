package by.gdev.util.os;

import by.gdev.util.OSInfo;

/**
 * Used to createOsExecutor special classes for every operation system
 */
public class OSExecutorFactoryMethod {
    private OSInfo.OSType osType = OSInfo.getOSType();

    public OSExecutor createOsExecutor() {
        switch (osType) {
            default:
            case WINDOWS:
                return new WindowsExecutor();
            case LINUX:
                return new LinuxExecutor();
            case MACOSX:
                return new MacExecutor();
        }
    }
}
