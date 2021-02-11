package desktop.starter.util.os;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.X11;
import desktop.starter.util.model.CUDAVersion;
import desktop.starter.util.model.GPUDescription;
import desktop.starter.util.model.GPUsDescriptionDTO;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LinuxExecutor implements OSExecutor {

    private static final Path CUDA_VERSION_PATH = Paths.get("/usr/local/cuda/version.txt");
    private static final String USER_AUTOSTART_FOLDER = ".config/autostart";
    private X11.Display dpy;
    private X11.Window win;
    private Xss.XScreenSaverInfo info;
    private boolean init = false;

    private void initVariable() {
        if (init)
            return;
        init = true;
        dpy = X11.INSTANCE.XOpenDisplay(null);
        if (Objects.isNull(dpy))
            return;
        win = X11.INSTANCE.XDefaultRootWindow(dpy);
        info = Xss.INSTANCE.XScreenSaverAllocInfo();
    }

    @Override
    public String execute(String command, int seconds) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor(seconds, TimeUnit.SECONDS);
        String res = IOUtils.toString(p.getInputStream(), Charset.defaultCharset());
        p.getInputStream().close();
        return res;
    }

    @Override
    public GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException {
        String res = execute("lshw -C display", 60);
        return getGPUInfo1(res, "product:");
    }

    @Override
    public CUDAVersion getCUDAVersion() throws IOException {
        String s = new String(Files.readAllBytes(CUDA_VERSION_PATH));
        String[] res = s.split(" ");
        if (res.length == 3) {
            Optional<CUDAVersion> op = Arrays.stream(CUDAVersion.values()).
                    filter(f -> res[2].startsWith(f.getValue())).findFirst();
            if (op.isPresent()) {
                return op.get();
            }
        }
        return null;
    }

    @Override
    public int getSystemHibernateDelay() {
        return 0;
    }

    @Override
    public boolean isIdleWithoutInputEventsMoreThan(int seconds) {
        initVariable();
        if (Objects.isNull(dpy))
            return true;
        Xss.INSTANCE.XScreenSaverQueryInfo(dpy, win, info);
        return info.idle.longValue() / 1000 > seconds;
    }

    @Override
    public boolean isIdleWithoutExecutionStateMoreThan(int seconds) {
        return true;
    }

    @Override
    public int setThreadExecutionState(int code) {
        return 0;
    }

    @SuppressWarnings("WeakerAccess")
    protected GPUsDescriptionDTO getGPUInfo1(String res, String stringStart) {
        String[] params = res.split(System.lineSeparator());
        List<GPUDescription> gpus = Arrays.stream(params).map(String::toLowerCase).
                filter(e -> e.contains(stringStart)).
                map(s -> {
                    GPUDescription g = new GPUDescription();
                    g.setName(s.split(":")[1]);
                    return g;
                }).collect(Collectors.toList());
        GPUsDescriptionDTO gpusDescriptionDTO = new GPUsDescriptionDTO();
        gpusDescriptionDTO.setRawDescription(res);
        gpusDescriptionDTO.setGpus(gpus);
        return gpusDescriptionDTO;
    }

    interface Xss extends Library {
        Xss INSTANCE = Native.load("Xss", Xss.class);

        XScreenSaverInfo XScreenSaverAllocInfo();

        int XScreenSaverQueryInfo(X11.Display dpy, X11.Drawable drawable,
                                  XScreenSaverInfo saver_info);

        class XScreenSaverInfo extends Structure {
            public X11.Window window; /* screen saver window */
            public int state; /* ScreenSaver{Off,On,Disabled} */
            public int kind; /* ScreenSaver{Blanked,Internal,External} */
            public NativeLong til_or_since; /* milliseconds */
            public NativeLong idle; /* milliseconds */
            public NativeLong event_mask; /* events */

            @Override
            protected List<String> getFieldOrder() {
                return Arrays.asList("window", "state", "kind", "til_or_since",
                        "idle", "event_mask");
            }
        }
    }

    @Override
    public void startUpAppWithSystem(Path startUpAppPath, Path folder, String name) throws IOException {
        Map<String, String> map = new HashMap<>();
        map.put("Type", "Application");
        map.put("Version", "1.0");
        map.put("Name", name);
        map.put("Path", folder.toString());
        map.put("Exec", "/usr/bin/java -jar " + startUpAppPath.toString());
        map.put("Terminal", "false");
        map.put("Categories", "Java;");
        Path desktopFile = Paths.get(System.getProperty("user.home"), USER_AUTOSTART_FOLDER, name + ".desktop");
        StringBuilder b = new StringBuilder().append("[Desktop Entry]").append(System.lineSeparator());
        map.entrySet().forEach(e -> b.append(e.getKey()).append("=").append(e.getValue()).append(System.lineSeparator()));
        Files.write(desktopFile, b.toString().getBytes());
    }

    @Override
    public void deactivateStartupAppWithSystem(String name) throws IOException {
        Files.deleteIfExists(Paths.get(System.getProperty("user.home"), USER_AUTOSTART_FOLDER, name + ".desktop"));
    }
}