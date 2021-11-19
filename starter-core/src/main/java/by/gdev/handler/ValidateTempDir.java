package by.gdev.handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openide.filesystems.FileUtil;

public class ValidateTempDir extends ValisatedEnviromentAbstract {

	@Override
	public boolean validate() throws IOException {
		Path folder = Paths.get(System.getProperty("java.io.tmpdir"));
			if (Files.isRegularFile(folder))
				Files.delete(folder);
			if (!Files.exists(folder))
				FileUtil.createFolder(folder.toFile());
			return true;
	}

	@Override
	public String getExceptionMessage() {
		return localizationBandle.getString("validateTempDirs");
	}
}