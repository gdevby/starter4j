package desktop.starter.component.factory;

import desktop.starter.model.OSInfo;

/**
 * Used to createOsExecutor special classes for every operation system
 */
public class FactoryMethod {
    private OSInfo.OSType osType = OSInfo.getOSType();

    public OSExecutor createOsExecutor() {
        switch (osType) {
            default:
            case WINDOWS:
                System.out.println("OSExecutor cre");
                return new WindowsExecutor();
            case LINUX:
                System.out.println("OSExecutor linux");
                return new WindowsExecutor();
        }
    }
}
