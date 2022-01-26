package by.gdev.handler;

import java.util.ResourceBundle;

import by.gdev.model.ExceptionMessage;
import lombok.AllArgsConstructor;

//@Slf4j
@AllArgsConstructor
public class ValidateUpdate implements ValidateEnvironment {

	ResourceBundle bundle;

	@Override
	public boolean validate() {
		// we can check again users have this problem or not
		/*
		 * OSExecutorFactoryMethod os = new OSExecutorFactoryMethod(); os.getOsType();
		 * if (os.getOsType() == OSType.WINDOWS) { try { boolean KB4515384Exists =
		 * os.createOsExecutor().execute("wmic qfe get HotFixID",
		 * 5).contains("KB4515384"); if (KB4515384Exists) return false; }catch
		 * (IOException | InterruptedException e) { log.error("Error", e); } }
		 */
		return true;
	}

	@Override
	public ExceptionMessage getExceptionMessage() {
		return new ExceptionMessage(bundle.getString("validate.update"));
	}
}