package by.gdev.handler;

import java.util.Objects;

public class ValidateTempNull extends AbstractBandle {

	@Override
	public boolean validate() {
		return Objects.nonNull(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public String getExceptionMessage() {
		return bundle.getString("validateTempNull");
				
	}
}