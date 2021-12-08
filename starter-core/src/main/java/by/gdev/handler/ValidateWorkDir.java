package by.gdev.handler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidateWorkDir extends ValisatedEnviromentAbstract {
	String workDir;
	
	
	@Override
	public boolean validate() {
		if (new File(workDir).exists()) {
			if (!Files.isWritable(Paths.get(workDir)) || !Files.isReadable(Paths.get(workDir)))
				return false;
		}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return localizationBandle.getString("validate.workdir");
	}
}