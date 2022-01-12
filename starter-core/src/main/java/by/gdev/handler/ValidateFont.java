package by.gdev.handler;

import java.util.ResourceBundle;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ValidateFont implements ValidateEnvironment {
	
	ResourceBundle bundle;
	
	@Override
	public boolean validate() {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
				log.error("Error", e);
			}
			return true;
	}

	@Override
	public String getExceptionMessage() {
		return bundle.getString("validate.font");
	}
}