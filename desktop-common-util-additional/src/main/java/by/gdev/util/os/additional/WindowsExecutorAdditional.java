package by.gdev.util.os.additional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.PowrProf;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;

import by.gdev.util.DesktopUtil;
import by.gdev.util.os.WindowsExecutor;
import mslinks.ShellLink;

public class WindowsExecutorAdditional extends WindowsExecutor implements OSExecutorAdditional{
	private final Memory mem = new Memory(WinDef.ULONG.SIZE);
    private long lastCheckExecutionState;
    private long lastExecutionStateNotIdle;
    private final String windowsStartUpFolder = "AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup";
    
    @Override
    public int getSystemHibernateDelay() {
        int size = new WinNT.SYSTEM_POWER_INFORMATION().size();
        Memory mem = new Memory(size);
        PowrProf.INSTANCE.CallNtPowerInformation(PowrProf.POWER_INFORMATION_LEVEL.SystemPowerInformation,
                null, 0, mem, (int) mem.size());
        WinNT.SYSTEM_POWER_INFORMATION powerInfo = new WinNT.SYSTEM_POWER_INFORMATION(mem);
        return powerInfo.TimeRemaining;
    }

    @Override
    public boolean isIdleWithoutInputEventsMoreThan(int seconds) {
        WinUser.LASTINPUTINFO lastinputinfo = new WinUser.LASTINPUTINFO();
        User32.INSTANCE.GetLastInputInfo(lastinputinfo);
        long time = Kernel32.INSTANCE.GetTickCount64() - (long) lastinputinfo.dwTime;
        return (time / 1000) > seconds;
    }

    @Override
    public boolean isIdleWithoutExecutionStateMoreThan(int seconds) {
        if ((System.currentTimeMillis() - lastCheckExecutionState) > 1000) {
            PowrProf.INSTANCE.CallNtPowerInformation(PowrProf.POWER_INFORMATION_LEVEL.SystemExecutionState,
                    null, 0, mem, (int) mem.size());
            if (mem.getInt(0) != 0)
                lastExecutionStateNotIdle = System.currentTimeMillis();
            lastCheckExecutionState = System.currentTimeMillis();
        }
        return (System.currentTimeMillis() - lastExecutionStateNotIdle) > seconds * 1000;
    }

    @Override
    public int setThreadExecutionState(int code) {
        return Kernel32.INSTANCE.SetThreadExecutionState(code);
    }
    @Override
	public void startUpAppWithSystem(Path startupAppPath, Path folder, String name) throws IOException {
		ShellLink sl = ShellLink.createLink(DesktopUtil.getJavaPathByHome(true)).setWorkingDir(folder.toString())
				.setCMDArgs("-Dfile.encoding=UTF-8 -jar \"" + startupAppPath.toString() + "\"");
		sl.saveTo(Paths.get(buildStartUpFolder().toString(), name + ".lnk").toString());
	}

	private Path buildStartUpFolder() {
		return Paths.get(System.getProperty("user.home"), windowsStartUpFolder).toAbsolutePath();
	}

	@Override
	public void deactivateStartupAppWithSystem(String name) throws IOException {
		Files.deleteIfExists(Paths.get(buildStartUpFolder().toString(), name + ".lnk"));
	}
}
