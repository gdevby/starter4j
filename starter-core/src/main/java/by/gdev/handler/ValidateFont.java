package by.gdev.handler;

import java.util.ResourceBundle;

import javax.swing.UIManager;

import by.gdev.model.ValidationExceptionMessage;
import by.gdev.util.DesktopUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidateFont implements ValidateEnvironment {

	ResourceBundle bundle;

	@Override
	public boolean validate() {
		DesktopUtil.initLookAndFeel();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ExceptionInInitializerError error) {
			if (error.getCause() instanceof IllegalArgumentException) {
				if (error.getCause().getMessage().contains("Text-specific LCD")) {
					return false;
				}
			}
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public ValidationExceptionMessage getExceptionMessage() {
		return new ValidationExceptionMessage(bundle.getString("validate.font"));
	}
}