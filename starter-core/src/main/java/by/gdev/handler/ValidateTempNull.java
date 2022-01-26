package by.gdev.handler;

import java.util.Objects;
import java.util.ResourceBundle;

import by.gdev.model.ExceptionMessage;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidateTempNull implements ValidateEnvironment {
	ResourceBundle bundle;

	@Override
	public boolean validate() {
		return Objects.nonNull(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public ExceptionMessage getExceptionMessage() {
		return new ExceptionMessage(bundle.getString("validate.tempnull")); 
	}
}