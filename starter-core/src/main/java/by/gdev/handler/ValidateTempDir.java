package by.gdev.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import by.gdev.model.ExceptionMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ValidateTempDir implements ValidateEnvironment {
	
	ResourceBundle bundle;
	
	@Override
	public boolean validate() {
		Path folder = Paths.get(System.getProperty("java.io.tmpdir"));
		try {
			if (Files.isRegularFile(folder))
				Files.delete(folder);
			if (!Files.exists(folder))
				Files.createDirectory(folder);
		} catch (IOException e) {
			log.error("Error", e);
		}
			return true;
	}

	@Override
	public ExceptionMessage getExceptionMessage() {
		return new ExceptionMessage(bundle.getString("validate.tempdir"));
	}
}