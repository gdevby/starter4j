package by.gdev.handler;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateFont extends ValisatedEnviromentAbstract {

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
		return localizationBandle.getString("validate.font");
	}
}