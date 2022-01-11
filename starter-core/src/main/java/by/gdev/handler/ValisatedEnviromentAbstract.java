package by.gdev.handler;

import java.util.ResourceBundle;

import lombok.Data;

@Data
public abstract class ValisatedEnviromentAbstract implements ValidateEnvironment {
	//TODO one object
	protected ResourceBundle localizationBandle = ResourceBundle.getBundle("application", new Localise().getLocal());
}
