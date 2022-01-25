package by.gdev.handler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import by.gdev.model.ExceptionMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidateWorkDir implements ValidateEnvironment {
	String workDir;
	ResourceBundle bundle;
	
 
	@Override
	public boolean validate() {
		if (new File(workDir).exists()) {
			if (!Files.isWritable(Paths.get(workDir)) || !Files.isReadable(Paths.get(workDir)))
				return false;
		}
		return true;
	}
	@Override
	public ExceptionMessage getExceptionMessage() {
		return new ExceptionMessage(String.format(bundle.getString("validate.workdir"), workDir));
	}
}