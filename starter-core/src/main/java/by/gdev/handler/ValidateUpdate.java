package by.gdev.handler;

import java.io.IOException;

import by.gdev.util.os.OSExecutorFactoryMethod;

public class ValidateUpdate extends AbstractBandle {

	@Override
	public boolean validate() throws IOException, InterruptedException {
		OSExecutorFactoryMethod os = new OSExecutorFactoryMethod();
		if (os.getOsType() == os.getOsType().WINDOWS) {
				boolean KB4515384Exists = os.createOsExecutor().execute("wmic qfe get HotFixID", 5).contains("KB4515384");
				if (KB4515384Exists) {
					return false;
				}
		}
		return true;
	}

	@Override
	public String getExceptionMessage() {
		return bundle.getString("validate.update");
	}
}