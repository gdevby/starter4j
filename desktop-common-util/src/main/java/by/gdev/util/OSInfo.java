package by.gdev.util;

import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OSInfo {
    private static final String OS_NAME = "os.name";
    private static final String OS_VERSION = "os.version";
    private static final PrivilegedAction<OSType> osTypeAction;
    private static final Map<String, WindowsVersion> windowsVersionMap = new HashMap<>();
    public static final WindowsVersion WINDOWS_UNKNOWN = new WindowsVersion(-1, -1);
    public static final WindowsVersion WINDOWS_95 = new WindowsVersion(4, 0);
    public static final WindowsVersion WINDOWS_98 = new WindowsVersion(4, 10);
    public static final WindowsVersion WINDOWS_ME = new WindowsVersion(4, 90);
    public static final WindowsVersion WINDOWS_2000 = new WindowsVersion(5, 0);
    public static final WindowsVersion WINDOWS_XP = new WindowsVersion(5, 1);
    public static final WindowsVersion WINDOWS_2003 = new WindowsVersion(5, 2);
    public static final WindowsVersion WINDOWS_VISTA = new WindowsVersion(6, 0);
    public static final WindowsVersion WINDOWS_7 = new WindowsVersion(6, 1);
    public static final WindowsVersion WINDOWS_8 = new WindowsVersion(6, 2);
    public static final WindowsVersion WINDOWS_8_1 = new WindowsVersion(6, 3);
    public static final WindowsVersion WINDOWS_10 = new WindowsVersion(10, 0);

    public enum OSType {
        WINDOWS,
        LINUX,
        SOLARIS,
        MACOSX,
        UNKNOWN
    }

    //todo added maybe raspberry
    public static OSType getOSType() throws SecurityException {
        String osName = System.getProperty(OS_NAME).toLowerCase(Locale.ROOT);
        if (osName != null) {
            if (osName.contains("windows")) {
                return OSType.WINDOWS;
            }

            if (osName.contains("os x") || osName.contains("mac")) {
                return OSType.MACOSX;
            }

            if (osName.contains("linux") || osName.contains("unix")) {
                return OSType.LINUX;
            }

            if (osName.contains("solaris") || osName.contains(("sunos"))) {
                return OSType.SOLARIS;
            }
        }

        return OSType.UNKNOWN;
    }
    public static Arch getJavaBit() {
		String res = System.getProperty("sun.arch.data.model");
		if (res != null && res.equalsIgnoreCase("64"))
			return Arch.x64;
		return Arch.x32;
	}

    public static PrivilegedAction<OSType> getOSTypeAction() {
        return osTypeAction;
    }

    public static WindowsVersion getWindowsVersion() throws SecurityException {
        String windowsVersion = System.getProperty(OS_VERSION);
        if (windowsVersion == null) {
            return WINDOWS_UNKNOWN;
        } else {
            synchronized (windowsVersionMap) {
                WindowsVersion currentVersion = windowsVersionMap.get(windowsVersion);
                if (currentVersion == null) {
                    String[] data = windowsVersion.split("\\.");
                    if (data.length != 2) {
                        return WINDOWS_UNKNOWN;
                    }

                    try {
                        currentVersion = new WindowsVersion(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                    } catch (NumberFormatException var6) {
                        return WINDOWS_UNKNOWN;
                    }

                    windowsVersionMap.put(windowsVersion, currentVersion);
                }

                return currentVersion;
            }
        }
    }

    static {
        windowsVersionMap.put(WINDOWS_95.toString(), WINDOWS_95);
        windowsVersionMap.put(WINDOWS_98.toString(), WINDOWS_98);
        windowsVersionMap.put(WINDOWS_ME.toString(), WINDOWS_ME);
        windowsVersionMap.put(WINDOWS_2000.toString(), WINDOWS_2000);
        windowsVersionMap.put(WINDOWS_XP.toString(), WINDOWS_XP);
        windowsVersionMap.put(WINDOWS_2003.toString(), WINDOWS_2003);
        windowsVersionMap.put(WINDOWS_VISTA.toString(), WINDOWS_VISTA);
        windowsVersionMap.put(WINDOWS_7.toString(), WINDOWS_7);
        windowsVersionMap.put(WINDOWS_8.toString(), WINDOWS_8);
        windowsVersionMap.put(WINDOWS_8_1.toString(), WINDOWS_8_1);
        windowsVersionMap.put(WINDOWS_10.toString(), WINDOWS_10);
        osTypeAction = OSInfo::getOSType;
    }

    public static class WindowsVersion implements Comparable<WindowsVersion> {
        private final int major;
        private final int minor;

        private WindowsVersion(int var1, int var2) {
            this.major = var1;
            this.minor = var2;
        }

        public int getMajor() {
            return this.major;
        }

        public int getMinor() {
            return this.minor;
        }

        public int compareTo(WindowsVersion version) {
            int major = this.major - version.getMajor();
            if (major == 0) {
                major = this.minor - version.getMinor();
            }

            return major;
        }

        public boolean equals(Object obj) {
            return obj instanceof WindowsVersion && this.compareTo((WindowsVersion) obj) == 0;
        }

        public int hashCode() {
            return 31 * this.major + this.minor;
        }

        public String toString() {
            return this.major + "." + this.minor;
        }
    }
	public enum Arch {
		x32, x64;
	}
}

