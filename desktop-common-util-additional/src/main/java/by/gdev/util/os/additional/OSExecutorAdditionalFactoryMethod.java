package by.gdev.util.os.additional;

import by.gdev.util.OSInfo;
import lombok.Data;

/**
 * Used to createOsExecutor special classes for every operation system
 */

@Data
public class OSExecutorAdditionalFactoryMethod {
    private OSInfo.OSType osType = OSInfo.getOSType();

    public OSExecutorAdditional createOsExecutorAdditional() {
        switch (osType) {
            default:
            case WINDOWS:
                return new WindowsExecutorAdditional();
            case LINUX:
                return new LinuxExecutorAdditional();
            case MACOSX:
                return new MacExecutorAdditional();
        }
    }
}
