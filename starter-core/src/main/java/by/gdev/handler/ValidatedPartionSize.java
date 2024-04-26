package by.gdev.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
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
	private ResourceBundle bundle;

	@Override
	public boolean validate() {
		System.out.println(workDir.toPath().getRoot());
		try {
			if (!workDir.exists())
				workDir.mkdirs();
			FileStore store = Files.getFileStore(Paths.get(workDir.getAbsolutePath()));
			long res = store.getUsableSpace();
			return res > minMemorySize * 1024 * 1024;
		} catch (IOException e) {
			log.error("Error", e);
		}
		return true;
	}
	
	@Override
	public ExceptionMessage getExceptionMessage() {
		Path disk = workDir.toPath().getRoot();
		return new ExceptionMessage(String.format(bundle.getString("validate.size"), disk , workDir.getFreeSpace() / 1024 / 1024, disk));
	}
}