package by.gdev.util.os.additional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import com.sun.jna.platform.unix.X11;

import by.gdev.util.os.LinuxExecutor;

/**
 * Have additional methods {@link OSExecutorAdditional}
 * 
 * @author Robert Makrytski
 *
 */
public class LinuxExecutorAdditional extends LinuxExecutor implements OSExecutorAdditional {
	private X11.Display dpy;
	private X11.Window win;
	private Xss.XScreenSaverInfo info;
	private boolean init = false;
	public static final String USER_AUTOSTART_FOLDER = ".config/autostart";

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
		map.entrySet()
				.forEach(e -> b.append(e.getKey()).append("=").append(e.getValue()).append(System.lineSeparator()));
		Files.write(desktopFile, b.toString().getBytes());
	}

	@Override
	public void deactivateStartupAppWithSystem(String name) throws IOException {
		Files.deleteIfExists(Paths.get(System.getProperty("user.home"), USER_AUTOSTART_FOLDER, name + ".desktop"));
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

	interface Xss extends Library {
		Xss INSTANCE = Native.load("Xss", Xss.class);

		XScreenSaverInfo XScreenSaverAllocInfo();

		int XScreenSaverQueryInfo(X11.Display dpy, X11.Drawable drawable, XScreenSaverInfo saver_info);

		class XScreenSaverInfo extends Structure {
			public X11.Window window; /* screen saver window */
			public int state; /* ScreenSaver{Off,On,Disabled} */
			public int kind; /* ScreenSaver{Blanked,Internal,External} */
			public NativeLong til_or_since; /* milliseconds */
			public NativeLong idle; /* milliseconds */
			public NativeLong event_mask; /* events */

			@Override
			protected List<String> getFieldOrder() {
				return Arrays.asList("window", "state", "kind", "til_or_since", "idle", "event_mask");
			}
		}
	}
}
