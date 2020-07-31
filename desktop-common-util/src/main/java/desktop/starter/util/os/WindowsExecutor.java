package desktop.starter.util.os;

import desktop.starter.util.model.CUDAVersion;
import desktop.starter.util.model.GPUDescription;
import desktop.starter.util.model.GPUsDescriptionDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WindowsExecutor implements OSExecutor {
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
}
