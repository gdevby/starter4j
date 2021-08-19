package by.gdev.handler;

import java.nio.file.Paths;
import java.util.Objects;

public class ValidateTempNull implements ValidateEnvironment {

	@Override
	public boolean valite() {
		return Objects.nonNull(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public String getExceptionMessage() {
		return "Доступ к директории /tmp невозможен. Свяжитесь с нами для решения дайнной проблемы";
	}
}