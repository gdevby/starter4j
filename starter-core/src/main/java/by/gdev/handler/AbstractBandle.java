package by.gdev.handler;

import java.util.ResourceBundle;

import lombok.Data;

@Data
public abstract class AbstractBandle implements ValidateEnvironment {
	protected ResourceBundle bundle = ResourceBundle.getBundle("application", new Localise().getLocal());
}
