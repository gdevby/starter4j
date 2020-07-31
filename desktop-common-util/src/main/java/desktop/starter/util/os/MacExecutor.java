package desktop.starter.util.os;

import desktop.starter.util.model.GPUsDescriptionDTO;
import java.io.IOException;

public class MacExecutor extends LinuxExecutor {

    @Override
    public GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException {
        String res = execute("system_profiler SPDisplaysDataType", 60);
        return getGPUInfo1(res, "chipset model:");
    }
}
