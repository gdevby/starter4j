package by.gdev.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openide.filesystems.FileUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateTempDir extends ValisatedEnviromentAbstract {

	@Override
	public boolean validate() {
		Path folder = Paths.get(System.getProperty("java.io.tmpdir"));
		try {
			if (Files.isRegularFile(folder))
				Files.delete(folder);
			if (!Files.exists(folder))
				FileUtil.createFolder(folder.toFile());
		} catch (IOException e) {
			log.error("Error", e);
		}
			return true;
	}

	@Override
	public String getExceptionMessage() {
		return localizationBandle.getString("validate.tempdir");
	}
}