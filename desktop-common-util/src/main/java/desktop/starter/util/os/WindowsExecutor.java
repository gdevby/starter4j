package desktop.starter.util.os;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.*;
import desktop.starter.util.model.CUDAVersion;
import desktop.starter.util.model.GPUDescription;
import desktop.starter.util.model.GPUsDescriptionDTO;
import mslinks.ShellLink;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/*
implemented services for win and linux
* */
public class WindowsExecutor implements OSExecutor {
    private final Memory mem = new Memory(WinDef.ULONG.SIZE);
    private final String windowsStartUpFolder = "AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup";
    private long lastCheckExecutionState;
    private long lastExecutionStateNotIdle;

    @Override
    public String execute(String command, int seconds) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("cmd.exe /C chcp 437 & " + command);
        p.waitFor(seconds, TimeUnit.SECONDS);
        String res = IOUtils.toString(p.getInputStream(), "IBM437");
        p.getInputStream().close();
        return res;
    }

    @Override
    public GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException {
        Path path = null;
        try {
            path = Files.createTempFile("dxdiag", ".txt");
            String command = String.format("dxdiag /whql:off /t %s", path.toAbsolutePath());
            execute(command, 60);
            //wait before file will be written full
            long size = -1;
            for(int i = 0; i < 60; i ++){
                Thread.sleep(500L);
                if(Files.exists(path))
                    if(size == path.toFile().length())
                        break;
                    else size = path.toFile().length();
            }
            List<String> list = Files.readAllLines(path, Charset.forName("437"));
            List<GPUDescription> gpus = new ArrayList<>();
            for(String s: list.stream().map(String::toLowerCase).collect(Collectors.toList())){
                if(StringUtils.contains(s,"card name:")){
                    GPUDescription g = new GPUDescription();
                    g.setName(s.split(":")[1]);
                    gpus.add(g);
                }
                if(gpus.size() > 0){
                    if(StringUtils.contains(s,"chip type:")){
                       gpus.get(gpus.size()-1).setChipType(s.split(":")[1]);
                    }else if(StringUtils.contains(s,"display memory:")){
                        gpus.get(gpus.size()-1).setMemory(s.split(":")[1]);
                    }
                }
            }
            GPUsDescriptionDTO GPUsDescriptionDTO1 = new GPUsDescriptionDTO();
            GPUsDescriptionDTO1.setGpus(gpus);
            return GPUsDescriptionDTO1;
        } finally {
            if (Objects.nonNull(path))
                Files.deleteIfExists(path);
        }
    }

    @Override
    public CUDAVersion getCUDAVersion() {
        return null;
    }

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
    public void startUpAppWithSystem(Path startUpAppPath, Path folder, String name) throws IOException {
        ShellLink sl = ShellLink.createLink(startUpAppPath.getFileName().toString())
                .setWorkingDir(folder.toString());
        sl.saveTo(Paths.get(buildStartUpFolder().toString(), name + ".lnk").toString());
        System.out.println(sl.getWorkingDir());
        System.out.println(sl.resolveTarget());
    }

    private Path buildStartUpFolder() {
        return Paths.get(System.getProperty("user.home"), windowsStartUpFolder).toAbsolutePath();
    }

    @Override
    public void deactivateStartupAppWithSystem(String name) throws IOException {
        Files.deleteIfExists(Paths.get(buildStartUpFolder().toString(), name + ".lnk"));
    }
}
