package desktop.starter.util;

import java.io.File;

public class DesktopUtil {

    public static File getSystemPath(OSInfo.OSType type, String path) {
        String userHome = System.getProperty("user.home", ".");
        File file;
        switch (type) {
            case LINUX:
            case SOLARIS:
                file = new File(userHome, path);
                break;
            case WINDOWS:
                String applicationData = System.getenv("APPDATA");
                String folder = applicationData != null ? applicationData : userHome;
                file = new File(folder, path);
                break;
            case MACOSX:
                file = new File(userHome, "Library/Application Support/" + path);
                break;
            default:
                file = new File(userHome, path);
        }
        return file;
    }
}
