package by.gdev.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidatedPartionSize extends ValisatedEnviromentAbstract {
	private long minMemorySize;
	private File workDir;

	public ValidatedPartionSize(long minMemorySize, File workDir) {
		this.minMemorySize = minMemorySize;
		this.workDir = workDir;
	}

	@Override
	public boolean validate() {
		try {
			if (!workDir.exists())
				workDir.mkdirs();
			FileStore store = Files.getFileStore(Paths.get(workDir.getAbsolutePath()));
			long res = store.getUsableSpace();
			return res > minMemorySize * 1024 * 1024;
		} catch (IOException e) {
			log.error("Error", e);
		}finally {
			workDir.deleteOnExit();
		}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return String.format(localizationBandle.getString("validate.size"), workDir , workDir.getFreeSpace() / 1024 / 1024, workDir);
		
		
	}
}