package by.gdev.util.os;

import java.io.IOException;

import by.gdev.util.model.GPUDriverVersion;
import by.gdev.util.model.GPUsDescriptionDTO;

/**
 * Used unique methods or info to get info about every os.
 */
public interface OSExecutor {
	String execute(String command, int seconds) throws IOException, InterruptedException;

	GPUsDescriptionDTO getGPUInfo() throws IOException, InterruptedException;

	GPUDriverVersion getGPUDriverVersion() throws IOException, InterruptedException;
}