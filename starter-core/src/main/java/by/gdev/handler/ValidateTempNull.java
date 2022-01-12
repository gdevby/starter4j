package by.gdev.handler;

import java.util.Objects;
import java.util.ResourceBundle;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidateTempNull implements ValidateEnvironment {
	ResourceBundle bundle;

	@Override
	public boolean validate() {
		return Objects.nonNull(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public String getExceptionMessage() {
		return bundle.getString("validate.tempnull");
				
	}
}