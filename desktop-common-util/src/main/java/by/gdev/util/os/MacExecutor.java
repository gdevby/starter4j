package by.gdev.util.os;

import java.io.IOException;

import by.gdev.util.model.GPUsDescriptionDTO;

public class MacExecutor extends LinuxExecutor {

	@Override
	public GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException {
		String res = execute("system_profiler SPDisplaysDataType", 60);
		return getGPUInfo1(res, "chipset model:");
	}

}
