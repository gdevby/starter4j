package by.gdev.handler;

import java.util.Objects;

public class ValidateTempNull extends ValisatedEnviromentAbstract {

	@Override
	public boolean validate() {
		return Objects.nonNull(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public String getExceptionMessage() {
		return localizationBandle.getString("validateTempNull");
				
	}
}