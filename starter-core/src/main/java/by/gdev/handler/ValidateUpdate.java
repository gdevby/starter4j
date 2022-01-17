package by.gdev.handler;

import java.io.IOException;
import java.util.ResourceBundle;

import by.gdev.model.ValidationExceptionMessage;
import by.gdev.util.OSInfo.OSType;
import by.gdev.util.os.OSExecutorFactoryMethod;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ValidateUpdate implements ValidateEnvironment {
	
	ResourceBundle bundle;

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
	public ValidationExceptionMessage getExceptionMessage() {
		return new ValidationExceptionMessage(bundle.getString("validate.update"));
	}
}