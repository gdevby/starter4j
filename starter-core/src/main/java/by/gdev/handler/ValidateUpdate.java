package by.gdev.handler;

import java.io.IOException;

import by.gdev.util.OSInfo.OSType;
import by.gdev.util.os.OSExecutorFactoryMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateUpdate extends ValisatedEnviromentAbstract {

	@Override
	public boolean validate() {
		OSExecutorFactoryMethod os = new OSExecutorFactoryMethod();
		os.getOsType();
		if (os.getOsType() == OSType.WINDOWS) {
				try {
					boolean KB4515384Exists = os.createOsExecutor().execute("wmic qfe get HotFixID", 5).contains("KB4515384");
					if (KB4515384Exists)
						return false;
				}catch (IOException | InterruptedException e) {
					log.error("Error", e);
				}
		}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return localizationBandle.getString("validate.update");
	}
}