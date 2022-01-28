package by.gdev.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import by.gdev.model.ExceptionMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ValidatedPartionSize implements ValidateEnvironment {
	private long minMemorySize;
	private File workDir;
	ResourceBundle bundle;

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
			//TODO why on exit?
			workDir.deleteOnExit();
		}
		return true;
	}
	
	@Override
	public ExceptionMessage getExceptionMessage() {
		return new ExceptionMessage(String.format(bundle.getString("validate.size"), workDir , workDir.getFreeSpace() / 1024 / 1024, workDir));
	}
}